package com.tae.bathtub.domain.interactor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.tae.bathtub.data.api.ServiceCallback;
import com.tae.bathtub.data.api.model.Boiler;
import com.tae.bathtub.data.api.model.ErrorResponse;

import org.json.JSONObject;

/**
 * Created by Eduardo on 18/04/2016.
 */
public interface BoilerInteractor {
    void getBoiler(ServiceCallback<Boiler, ErrorResponse> callback);
}
