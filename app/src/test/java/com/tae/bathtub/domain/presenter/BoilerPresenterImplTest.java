package com.tae.bathtub.domain.presenter;

import android.support.annotation.NonNull;

import com.tae.bathtub.data.api.ServiceCallback;
import com.tae.bathtub.data.api.model.Boiler;
import com.tae.bathtub.data.api.model.ErrorResponse;
import com.tae.bathtub.data.local.Bathtub;
import com.tae.bathtub.data.local.Tap;
import com.tae.bathtub.domain.interactor.BoilerInteractor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;


/**
 * Created by Eduardo on 19/04/2016.
 */
@RunWith(JUnit4.class)
public class BoilerPresenterImplTest {

    @Mock
    BoilerInteractor interactor;

    @Captor
    private ArgumentCaptor<ServiceCallback<Boiler,ErrorResponse>> boilerCallbackCaptor;

    private BoilerPresenterImpl presenter;
    private Boiler boiler;
    private ErrorResponse errorResponse;
    private int level;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        presenter = new BoilerPresenterImpl();
        presenter.interactor = interactor;
        boiler = new Boiler(50, 10);
        errorResponse = new ErrorResponse("Service fail");
        level = 0;
    }

    @Test
    public void presenter_shouldNotBeNull() throws Exception {
        assertNotNull(presenter);
    }

    @Test
    public void initBoilerService_should_success() throws Exception {
        presenter.initBoilerService();
        verify(interactor, times(1)).getBoiler(boilerCallbackCaptor.capture());
        boilerCallbackCaptor.getValue().onServiceResponse(boiler);
        assertThat(presenter.getColdWater(), is(equalTo(boiler.getCold_water())));
        assertThat(presenter.getHotWater(), is(equalTo(boiler.getHot_water())));
    }

    @Test
    public void initBoilerService_should_fail() throws Exception {
        presenter.initBoilerService();
        verify(interactor, times(1)).getBoiler(boilerCallbackCaptor.capture());
        boilerCallbackCaptor.getValue().onServiceError(errorResponse);
        assertEquals("There is no water!!", "Service fail", presenter.getServiceError(errorResponse.getError()));
    }

    @Test
    public void testGetHotWater() throws Exception {
        initBoilerService_should_success();
        assertEquals("Hot water is 50", 50, presenter.getHotWater());
    }

    @Test
    public void testGetColdWater() throws Exception {
        initBoilerService_should_success();
        assertEquals("Cold water is 10", 10, presenter.getColdWater());
    }

    @Test
    public void waterTemperature_shouldBeResultOfTwoTapsOpen() throws Exception {
        initBoilerService_should_success();
        Bathtub bathtub = new Bathtub();
        List<Tap> taps = new ArrayList<>(2);
        taps.add(new Tap(Tap.Type.COLD.name(), 10, true));
        taps.add(new Tap(Tap.Type.HOT.name(), 50, true));
        bathtub.setTaps(taps);
        assertTrue(bathtub.areTwoTapsOpen(taps));
        assertEquals("Temperature is  30", 30,
                bathtub.getTemperatureFromTaps(taps.get(0).getTemperature(),taps.get(1).getTemperature()));
    }

    @Test
    public void waterTemperature_shouldReturnColdAndHot() throws Exception {
        initBoilerService_should_success();
        Bathtub bathtub = getBathtub();
        assertTrue(bathtub.getTaps().get(0).isOpen());
        assertEquals("Temperature is  10", 10, bathtub.getTaps().get(0).getTemperature());
        assertTrue(bathtub.getTaps().get(1).isOpen());
        assertEquals("Temperature is  50", 50, bathtub.getTaps().get(1).getTemperature());

    }

    @NonNull
    private Bathtub getBathtub() {
        Bathtub bathtub = new Bathtub();
        List<Tap> taps = new ArrayList<>(2);
        taps.add(new Tap(Tap.Type.COLD.name(), 10, true));
        taps.add(new Tap(Tap.Type.HOT.name(), 50, true));
        bathtub.setTaps(taps);
        return bathtub;
    }

}