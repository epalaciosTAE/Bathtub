package com.tae.bathtub.domain.presenter;

import android.os.Handler;
import android.util.Log;

import com.tae.bathtub.data.api.ServiceCallback;
import com.tae.bathtub.data.api.model.Boiler;
import com.tae.bathtub.data.api.model.ErrorResponse;
import com.tae.bathtub.data.local.Bathtub;
import com.tae.bathtub.data.local.Tap;
import com.tae.bathtub.domain.interactor.BoilerInteractor;
import com.tae.bathtub.presentation.BathtubView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by Eduardo on 18/04/2016.
 */
public class BoilerPresenterImpl implements BoilerPresenter {

    @Inject
    BoilerInteractor interactor;
    @Inject
    BathtubView view;

    private Boiler boiler;
    private Bathtub bathtub;
    private Subscription openTapSubscription;
    private Subscription dobleTapSubscription;

    @Inject
    public BoilerPresenterImpl() {
    }

    @Override
    public void initBoilerService() {
        interactor.getBoiler(new ServiceCallback<Boiler, ErrorResponse>() {
            @Override
            public void onServiceResponse(Boiler response) {
                boiler = response;
            }

            @Override
            public void onServiceError(ErrorResponse error) {
                getServiceError(error.getError());
            }
        });
    }

    @Override
    public int getHotWater() {
        return boiler.getHot_water();
    }

    @Override
    public int getColdWater() {
        return boiler.getCold_water();
    }

    @Override
    public String getServiceError(String error) {
        return error;
    }

    @Override
    public void unSubscribeSingleTap() {
        if (openTapSubscription != null &&!openTapSubscription.isUnsubscribed()) {
            openTapSubscription.unsubscribe();
            Log.i("CLOSE TAP", "unSubscribeSingleTap: tap is close!");
        }
    }

    @Override
    public void openBothTaps() {
        Observable<Long> tapCold = Observable.interval(3, TimeUnit.SECONDS);
        Observable<Long> tapHot = Observable.interval(3, TimeUnit.SECONDS);
        dobleTapSubscription = tapCold.zipWith(tapHot, new Func2<Long, Long, Integer>() {
            @Override
            public Integer call(Long intervalCold, Long intervalHot) {
                Log.i("ZIP", "call: along1 " + intervalCold + " along2 " + intervalHot);
                // TODO eacch observable should handle different behaviours between the tap. Untils that its hardcoded.
                return 22; //10 cold 12 hot
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        updateUiAfterComplete(dobleTapSubscription);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("DOBLE TAP ERROR", "onError: ", e);
                    }

                    @Override
                    public void onNext(final Integer waterLevel) {
                        Log.i("DOBLE TAP NEXT", "onNext: waterlevel both taps");
                        handleOnNext(waterLevel);
                        if (bathtub.getLevel() >= Bathtub.MAX_CAPACITY) {
                            updateUiWithWaterLevelOverflow();
                            onCompleted();
                        }
                    }
                });
    }

    @Override
    public void openSingleTap(final Tap tap) {
        Observable<Long> waterlevelObservable = Observable.interval(3, TimeUnit.SECONDS);
        openTapSubscription = waterlevelObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        updateUiAfterComplete(openTapSubscription);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("SINGLE TAP ERROR", "onError: ", e);
                    }

                    @Override
                    public void onNext(Long interval) {
                        int waterStream = getWaterStreamByTapType(tap);
                        handleOnNext(waterStream);
                        if (bathtub.getLevel() >= Bathtub.MAX_CAPACITY) {
                            updateUiWithWaterLevelOverflow();
                            onCompleted();
                        }
                    }
                });
    }

    @Override
    public void unSubscribeTaps() {
        if (dobleTapSubscription != null && !dobleTapSubscription.isUnsubscribed()) {
            dobleTapSubscription.unsubscribe();
            Log.i("CLOSE TAPS", "Both taps are closed!");
        }
    }

    @Override
    public void getBathtub(Bathtub bathtub) {
        this.bathtub = bathtub;
    }

    private void updateUiWithWaterLevelOverflow() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.waterLevelOverflow(bathtub.getLevel() >= Bathtub.MAX_CAPACITY);
            }
        });
    }

    private int getWaterStreamByTapType(Tap tap) {
        int waterStream;
        if (tap.getType().equals(Tap.Type.COLD.name())) {
            waterStream = 10;
        } else {
            waterStream = 12;
        }
        return waterStream;
    }

    private void handleOnNext(int waterStream) {
        bathtub.setLevel(bathtub.getLevel() + waterStream);
        calculateTemperature(bathtub);
        view.increaseWaterLevel(convertIntToNegative());
    }

    private void updateUiAfterComplete(Subscription subscription) {
        subscription.unsubscribe();
        Schedulers.shutdown();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.increaseWaterLevel(convertIntToNegative());
                view.showToast("Bathub is full, taps are disabled, enjoy!");
                view.setTemperatureIndicator(getIndicatorPosition(bathtub.getTemperature()));
            }
        });
    }

    private void calculateTemperature(Bathtub bathtub) {
        int temp = 0;
        if (bathtub.areTwoTapsOpen(bathtub.getTaps())) {
            temp = bathtub.getTemperatureFromTaps(getColdWater(), getHotWater());
        } else {
            for (Tap tap : bathtub.getTaps()) {
                if (tap.isOpen()) {
                    if (tap.getType().equals(Tap.Type.COLD.name())) {
                        temp = getColdWater();
                    } else {
                        temp = getHotWater();
                    }
                }
            }
        }
        if (temp > 0) {
            bathtub.setTemperature(temp);
            view.displayTemperature(temp);
            view.setTemperatureIndicator(getIndicatorPosition(temp));
        }
    }

    private float getIndicatorPosition(int temp) {
        float indicator = 0;
        switch (temp) {
            case 10:
                indicator = -45;
                break;
            case 50:
                indicator = 45;
                break;
            case 30:
                indicator = 25;
                break;
        }
        return indicator;
    }

    private int convertIntToNegative() {
        return bathtub.getLevel()*-1;
    }

}
