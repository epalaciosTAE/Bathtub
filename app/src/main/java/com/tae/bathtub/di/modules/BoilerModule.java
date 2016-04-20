package com.tae.bathtub.di.modules;

import com.tae.bathtub.di.ActivityScope;
import com.tae.bathtub.presentation.BathtubView;
import com.tae.bathtub.domain.interactor.BoilerInteractor;
import com.tae.bathtub.domain.interactor.BoilerInteractorImpl;
import com.tae.bathtub.domain.presenter.BoilerPresenter;
import com.tae.bathtub.domain.presenter.BoilerPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Eduardo on 18/04/2016.
 */
@Module
public class BoilerModule {

    private BathtubView view;

    public BoilerModule(BathtubView view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    BathtubView providesBathtubView() {
        return view;
    }

    @ActivityScope
    @Provides
    BoilerPresenter providesBoilerPresenter(BoilerPresenterImpl presenter) {
        return presenter;
    }

    @ActivityScope
    @Provides
    BoilerInteractor providesBoilerInteractor(BoilerInteractorImpl interactor) {
        return interactor;
    }

}
