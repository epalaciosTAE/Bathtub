package com.tae.bathtub.data.api;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Eduardo on 18/04/2016.
 */
public class ToStringConverterFactory  extends Converter.Factory  {

    private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

    @Inject
    public ToStringConverterFactory() {
    }

    public Converter<ResponseBody, ?> fromResponseBody(Type type, Annotation[] annotations) {
        if (String.class.equals(type)) {
            return new Converter<ResponseBody, String>() {
                @Override public String convert(ResponseBody value) throws IOException {
                    return value.string();
                }
            };
        }
        return null;
    }

    public Converter<?, RequestBody> toRequestBody(Type type, Annotation[] annotations) {
        if (String.class.equals(type)) {
            return new Converter<String, RequestBody>() {
                @Override public RequestBody convert(String value) throws IOException {
                    return RequestBody.create(MEDIA_TYPE, value);
                }
            };
        }
        return null;
    }
}
