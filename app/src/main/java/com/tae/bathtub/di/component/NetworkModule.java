package com.tae.bathtub.di.component;

import com.google.gson.FieldNamingPolicy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tae.bathtub.data.api.BathTubService;
import com.tae.bathtub.data.api.NetworkConstants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * Created by Eduardo on 18/04/2016.
 */
@Module
public class NetworkModule {

    @Singleton
    @Provides
    BathTubService providesBathTubService(Retrofit retrofit) {
        return retrofit.create(BathTubService.class);
    }

    @Singleton
    @Provides
    Retrofit providesRetrofit(OkHttpClient client, Gson gson, RxJavaCallAdapterFactory rxJavaCallAdapter) {
        return new Retrofit.Builder()
                .baseUrl(NetworkConstants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(rxJavaCallAdapter)
                .build();
    }

    @Singleton
    @Provides
    OkHttpClient providesOkHttpClient(HttpLoggingInterceptor interceptor) {
        return new OkHttpClient.Builder().addInterceptor(interceptor).build();
    }

    @Singleton
    @Provides
    Gson providesGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return builder.create();
    }

    @Singleton
    @Provides
    HttpLoggingInterceptor providesHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    @Singleton
    @Provides
    public RxJavaCallAdapterFactory providesRxJavaCallAdapterFactory() {
        return RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
    }

}
