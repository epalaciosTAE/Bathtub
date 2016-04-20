package com.tae.bathtub;

import android.app.Application;

import com.tae.bathtub.di.component.ApplicationComponent;
import com.tae.bathtub.di.component.DaggerApplicationComponent;
import com.tae.bathtub.di.component.NetworkModule;
import com.tae.bathtub.di.modules.ApplicationModule;

/**
 * Created by Eduardo on 18/04/2016.
 */
public class App extends Application {

    public static App instance;
    public ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        resolveDependency();
    }

    public static App getInstance() {
        return instance;
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    private void resolveDependency() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule())
                .networkModule(new NetworkModule())
                .build();
    }
}
