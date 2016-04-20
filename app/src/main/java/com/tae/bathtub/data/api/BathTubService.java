package com.tae.bathtub.data.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.tae.bathtub.data.api.model.Boiler;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Headers;
import rx.Observable;

/**
 * Created by Eduardo on 18/04/2016.
 */
public interface BathTubService {

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @GET(NetworkConstants.BOILER_END_POINT)
    Observable<Boiler> getBoiler();

//    Call<Boiler> getBoiler();
}
