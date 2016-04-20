package com.tae.bathtub.domain.interactor;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.tae.bathtub.data.api.BathTubService;
import com.tae.bathtub.data.api.ServiceCallback;
import com.tae.bathtub.data.api.model.Boiler;
import com.tae.bathtub.data.api.model.ErrorResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Eduardo on 18/04/2016.
 */
public class BoilerInteractorImpl implements BoilerInteractor {

    @Inject
    BathTubService service;

    @Inject
    public BoilerInteractorImpl() {
    }

    @Override
    public void getBoiler(final ServiceCallback<Boiler, ErrorResponse> callback) {
        // This was the initial approach using Retrofit, but as the JSON has missing the "" in the properties,
        //the converter is returning a malforming json exception.
        Observable<Boiler> boilerObservable = service.getBoiler();
        boilerObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boiler>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onServiceError(new ErrorResponse(e.getMessage()));
                        Log.i("ERROR", "onError: ", e);
                    }

                    @Override
                    public void onNext(Boiler boiler) {
                        callback.onServiceResponse(boiler);
                        Log.i("SUCCESS", "onNext: ");
                    }
                });

        //This is the second approach, a dirty Asynctask with an HttpUrlConection. No rotation handled, screen locked.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                HttpURLConnection urlConnection;
                try {
                    URL url = new URL("http://static.content.akqa.net/mobile-test/bath.json");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    int status = urlConnection.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201:
                            InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
                            BufferedReader br = new BufferedReader(isr);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            Gson gson = new Gson();
                            Boiler boiler = gson.fromJson(sb.toString(), Boiler.class);
                            callback.onServiceResponse(boiler);
                            break;
                        case 401:
                            callback.onServiceError(new ErrorResponse("Forbidden"));
                            break;
                        case 503:
                            callback.onServiceError(new ErrorResponse("Server Error"));
                            break;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
            }
                return null;
            }
        }.execute();
    }
}
