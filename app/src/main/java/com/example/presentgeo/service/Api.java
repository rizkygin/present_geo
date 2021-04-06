package com.example.presentgeo.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    public static Retrofit retrofit = null;
    public static Client getClient() {

//        url  = "http://10.10.30.64/eoffice_palangkaraya/api/";
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getURL()+ "api/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        //Creating object for our interface
        Client api = retrofit.create(Client.class);
        return api; // return the APIInterface object
    }

    public static String getURL(){
        String url = "http://10.10.30.39//eoffice_palangkaraya/";
        return url;
    }
    public static String getAsset(){
        String urlAsset = "assets/logo/";
        return urlAsset;
    }
}
