package com.crycetruly.dailydreams.Retro;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroClient {
    private static Retrofit instance;
    static String baseURL = "http://localhost:8080/days";

    public static Retrofit getRetrofit() {
        if (instance == null)
            instance = new Retrofit.Builder().baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create()
                    ).build();

            return instance;

    }
}
