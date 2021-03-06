package com.tae.bathtub.presentation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.tae.bathtub.App;
import com.tae.bathtub.R;
import com.tae.bathtub.data.local.Bathtub;
import com.tae.bathtub.data.local.Tap;
import com.tae.bathtub.di.component.DaggerBoilerComponent;
import com.tae.bathtub.di.modules.BoilerModule;
import com.tae.bathtub.domain.presenter.BoilerPresenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Flow:
 * For get boiler:
 * Activity --> Presenter --> Interactor --> callback to Presenter
 *
 * For User input:
 * Presenter --> do some logic --> BathtubView to update the view
 *
 * When the bathtub is full the taps are disabled.
 */
public class MainActivity extends AppCompatActivity implements BathtubView {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.indicator)ImageView imgIndicator;
    @Bind(R.id.imgBath) ImageView imgBathTub;
    @Bind({R.id.hotTap, R.id.coldTap}) List<ImageView>tapsView;

    @Inject BoilerPresenter presenter;

    private float currentLevel;
    private Tap coldTap, hotTap;
    private List<Tap> taps;
    private Bathtub bathtub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        resolveDependency();
        presenter.initBoilerService();
        bathtub = createBathtub();
        presenter.getBathtub(bathtub);
    }

    private Bathtub createBathtub() {
        coldTap = new Tap();
        hotTap = new Tap();
        taps = new ArrayList<>(2);
        taps.add(coldTap);
        taps.add(hotTap);
        return new Bathtub(taps);
    }

    @OnClick({R.id.coldTap, R.id.hotTap})
    void onClick(View view) {
        float rotateDirection;
        if (view.getId() == R.id.coldTap) {
            coldTap.setType(Tap.Type.COLD.name());
            coldTap.setTemperature(presenter.getColdWater());
            rotateDirection = -45f;
            handleTapEvents(view, rotateDirection, coldTap);
        } else {
            hotTap.setType(Tap.Type.HOT.name());
            hotTap.setTemperature(presenter.getHotWater());
            rotateDirection = 45f;
            handleTapEvents(view, rotateDirection, hotTap);
        }

        if (bathtub.areTwoTapsOpen(taps)) {
            presenter.unSubscribeSingleTap();
            presenter.openBothTaps();
        } else {
            for (Tap tap : taps) {
                if (tap.isOpen()) {
                    presenter.unSubscribeTaps();
                    presenter.openSingleTap(tap);
                }
            }
        }
    }

    private void handleTapEvents(View view, float rotateDirection, Tap tap) {
        if (!tap.isOpen()) {
            rotateIndicator(rotateDirection);
            rotateTap(view, 360f);
            tap.setOpen(true);
        } else {
            rotateIndicator(0f);
            tap.setOpen(false);
            rotateTap(view, -360f);
            presenter.unSubscribeSingleTap();
        }
    }

    @Override
    public void waterLevelOverflow(boolean overflow) {
        if (overflow) {
            ButterKnife.apply(tapsView, new ButterKnife.Action<ImageView>() {
                @Override
                public void apply(ImageView view, int index) {
                    Log.i(TAG, "apply: taps disabled");
                    view.setEnabled(false);
                    rotateIndicator(0f);
                }
            });
        }
    }

    @Override
    public void increaseWaterLevel(float level) {
        currentLevel = level;
        Log.i(TAG, "increaseWaterLevel: level value " + level);
        raiseWaterLevel(level);
    }

    @Override
    public void displayTemperature(int temp) {
        Log.i(TAG, "displayTemperature: Bathtub temperature is  + temp");
    }

    @Override
    public void setTemperatureIndicator(float indicatorPosition) {
        rotateIndicator(indicatorPosition);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unSubscribeSingleTap();
    }

    private void raiseWaterLevel(float level) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, currentLevel, level);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(500);
        animation.setFillAfter(true);
        imgBathTub.startAnimation(animation);
        currentLevel = level;
    }


    private void rotateTap(View view, float rotation) {
        RotateAnimation rotateAnimation = new RotateAnimation(0f, rotation, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(500);
        view.startAnimation(rotateAnimation);
    }

    private void rotateIndicator(float rotateDirection) {
        RotateAnimation rotateAnimation = new RotateAnimation(0f, rotateDirection, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(500);
        rotateAnimation.setFillAfter(true);
        imgIndicator.startAnimation(rotateAnimation);
    }

    private void resolveDependency() {
        DaggerBoilerComponent.builder()
                .applicationComponent(App.getInstance().getApplicationComponent())
                .boilerModule(new BoilerModule(this))
                .build()
                .inject(this);
    }
}
