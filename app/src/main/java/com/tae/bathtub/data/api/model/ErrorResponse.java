package com.tae.bathtub.data.api.model;

/**
 * Created by Eduardo on 18/04/2016.
 */
public class ErrorResponse {
    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
