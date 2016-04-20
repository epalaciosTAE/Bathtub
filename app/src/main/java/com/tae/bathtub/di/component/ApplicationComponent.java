package com.tae.bathtub.di.component;

import com.tae.bathtub.data.api.BathTubService;
import com.tae.bathtub.di.modules.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Eduardo on 18/04/2016.
 */
@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class})
public interface ApplicationComponent {
    BathTubService getBathTubService();
}
