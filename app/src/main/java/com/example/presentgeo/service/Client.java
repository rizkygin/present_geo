package com.example.presentgeo.service;

import com.example.presentgeo.model.ImeiRespon;
import com.example.presentgeo.model.JasonRekap;
import com.example.presentgeo.model.JsonMarker;
import com.example.presentgeo.model.JsonRespon;
import com.example.presentgeo.model.JsonUser;
import com.example.presentgeo.model.Parameter;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Client {
    @FormUrlEncoded
    @POST("login_api")
    Call<JsonUser> login(@Header("Authorization") String authHeader,
                         @Field("imei") String imei);

    @GET("param_api")
    Call<JsonMarker> present();

    @FormUrlEncoded
    @POST("absen_api")
    Call<JsonRespon> absen(@Field("id_pegawai") String id_pegawai,
                           @Field("latitude") Double latitude,
                           @Field("longitude") Double longitude,
                           @Field("imei")String imei);

    @GET("parameter_api")
    Call<Parameter> param();

    @POST("sign_imei_code")
    Call<ImeiRespon> sendImei(@Field("id_pegawai") String id_pegawai,
                              @Field("imei")String imei);
    @FormUrlEncoded
    @POST("jadwal")
    Call<JasonRekap> getRekap(@Field("id_pegawai")String id_pegawai);

}
