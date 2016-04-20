package com.tae.bathtub.domain.presenter;

import android.os.Handler;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import com.tae.bathtub.data.api.ServiceCallback;
import com.tae.bathtub.data.api.model.Boiler;
import com.tae.bathtub.data.api.model.ErrorResponse;
import com.tae.bathtub.data.local.Bathtub;
import com.tae.bathtub.data.local.Tap;
import com.tae.bathtub.presentation.BathtubView;
import com.tae.bathtub.domain.interactor.BoilerInteractor;

import java.util.ArrayList;
import java.util.List;
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

    private int level;
    private Boiler boiler;
    private Bathtub bathtub;
    private Subscription openTapSubscription;
    private Subscription dobleTapSubscription;
    private SparseArrayCompat<Float> levels;

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
        levels = generateWaterLevels();
    }

    @Override
    public int getHotWater() {
        return boiler.getHot_water();
    }

    @Override
    public int getColdWater() {
        return boiler.getCold_water();
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
    public void openBothTaps(List<Tap> taps) {
        Observable<Long> tapCold = Observable.interval(3, TimeUnit.SECONDS);
        Observable<Long> tapHot = Observable.interval(3, TimeUnit.SECONDS);
        dobleTapSubscription = tapCold.zipWith(tapHot, new Func2<Long, Long, Float>() {
            @Override
            public Float call(Long intervalCold, Long intervalHot) {
                Log.i("ZIP", "call: along1 " + intervalCold + " along2 " + intervalHot);
                float cold = convertToNegative(getWaterLevelByInterval(intervalCold));
                float hot = convertToNegative(getWaterLevelByInterval(intervalHot));
                bathtub.setLevel(bathtub.getLevel() + 20);
                final float waterLevel = cold + hot;
//                calculateTemperature(bathtub);


//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.i("SEND DATA TO ACTIVITY", "run: water level " + waterLevel);
//                        view.increaseWaterLevel(waterLevel);
//                    }
//                });
                return waterLevel;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Float>() {
                    @Override
                    public void onCompleted() {
                        dobleTapSubscription.unsubscribe();
                        Schedulers.shutdown();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                view.increaseWaterLevel(-220);
                                view.showToast("Bathub is full, taps are disabled, enjoy!");
                                view.setTemperatureIndicator(getIndicatorPosition(bathtub.getTemperature()));
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("DOBLE TAP ERROR", "onError: ", e);
                    }

                    @Override
                    public void onNext(final Float waterLevel) {
                        Log.i("DOBLE TAP NEXT", "onNext: waterlevel both taps");
                        calculateTemperature(bathtub);
                        view.increaseWaterLevel(waterLevel);
                        if (waterLevel*-1 >= Bathtub.MAX_CAPACITY) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    view.waterLevelOverflow(waterLevel*-1 >= Bathtub.MAX_CAPACITY);
                                }
                            });
                            onCompleted();
                        }
                    }
                });
    }

    @Override
    public void openSingleTap(Tap tap) {
        Observable<Long> waterlevelObservable = Observable.interval(3, TimeUnit.SECONDS);
        openTapSubscription = waterlevelObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        Schedulers.shutdown();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                view.increaseWaterLevel(-220);
                                view.showToast("Bathub is full, taps are disabled, enjoy!");
                                view.setTemperatureIndicator(getIndicatorPosition(bathtub.getTemperature()));
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long interval) {
                        float tempWaterLevel = convertToNegative(getWaterLevelByInterval(interval));
                        bathtub.setLevel(bathtub.getLevel() + 10);
                        calculateTemperature(bathtub);
                        final float waterLevel = tempWaterLevel;
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("SEND DATA TO ACTIVITY", "run: water level " + waterLevel);
                                view.increaseWaterLevel(waterLevel);
                            }
                        });

                        if (bathtub.getLevel() >= Bathtub.MAX_CAPACITY) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    view.waterLevelOverflow(level >= Bathtub.MAX_CAPACITY);
                                }
                            });
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

    private float getWaterLevelByInterval(Long interval) {
        return levels.get(interval.intValue());
    }

    private SparseArrayCompat<Float> generateWaterLevels() {
        SparseArrayCompat<Float> levels = new SparseArrayCompat<>();
        float increase = 0f;
        for (int i = 0; i < 15; i++) { //Using Rx instead of 15 was 3
//            increase += 73.3;
            increase += 14.6; // this float represents the value needed for the animation
            levels.put(i,increase);
        }
        return levels;
    }

    private float convertToNegative(float value) {
        return value * -1;
    }
}
