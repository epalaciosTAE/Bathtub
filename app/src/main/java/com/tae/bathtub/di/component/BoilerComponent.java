package com.tae.bathtub.di.component;

import com.tae.bathtub.di.ActivityScope;
import com.tae.bathtub.di.modules.BoilerModule;
import com.tae.bathtub.presentation.MainActivity;

import dagger.Component;

/**
 * Created by Eduardo on 18/04/2016.
 */
@ActivityScope
@Component(modules = BoilerModule.class, dependencies = ApplicationComponent.class)
public interface BoilerComponent {
    void inject(MainActivity activity);
}
