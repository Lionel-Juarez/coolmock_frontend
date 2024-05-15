package com.example.hamacav1.util;

import android.content.Context;

import okhttp3.OkHttpClient;

public class OkHttpProvider {

    private static OkHttpClient instance;

    public static OkHttpClient getInstance(Context context) {
        if (instance == null) {
            instance = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();
        }
        return instance;
    }
}
