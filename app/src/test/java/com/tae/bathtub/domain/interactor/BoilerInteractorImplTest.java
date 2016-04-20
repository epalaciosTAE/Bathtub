package com.tae.bathtub.domain.interactor;

import com.tae.bathtub.data.api.BathTubService;
import com.tae.bathtub.data.api.ServiceCallback;
import com.tae.bathtub.data.api.model.Boiler;
import com.tae.bathtub.data.api.model.ErrorResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Eduardo on 18/04/2016.
 */
@RunWith(JUnit4.class)
public class BoilerInteractorImplTest {

    @Mock
    BathTubService service;

    @Mock
    ServiceCallback<Boiler, ErrorResponse> callback;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void server_shouldReturnBoiler() throws Exception {
        when(service.getBoiler()).thenReturn(Observable.just(new Boiler(50, 10)));

        Observable<Boiler> observable = service.getBoiler();
        TestSubscriber<Boiler> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        assertNotNull(observable);
        testSubscriber.assertNoErrors();

        List<Boiler> taps = testSubscriber.getOnNextEvents();
        for (Boiler tap : taps) {
            assertNotNull(tap);
            assertEquals("hotTab water is 50", 50, tap.getHot_water());
            assertEquals("coldTab water is 10", 10, tap.getCold_water());
        }

    }

}
