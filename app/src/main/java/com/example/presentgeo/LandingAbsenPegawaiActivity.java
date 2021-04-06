package com.example.presentgeo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.presentgeo.model.JasonRekap;
import com.example.presentgeo.model.JsonMarker;
import com.example.presentgeo.model.JsonRespon;
import com.example.presentgeo.model.PolygonMarker;
import com.example.presentgeo.service.Api;
import com.example.presentgeo.service.LogoutSessionTimeOut;
import com.example.presentgeo.service.SessionTimeout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.maps.android.PolyUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandingAbsenPegawaiActivity extends AppCompatActivity implements LogoutSessionTimeOut {

    private static final String TAG = "LandingPegawai";
    MaterialTextView eDate,eInstansi,eTahun,eNip,eMasuk,ePulang,eKet;
    SharedPreferences sharedPreferences;
    String id_pegawai,hari,bulan;
    ExtendedFloatingActionButton fabAbsen;
    List<LatLng> mList = new ArrayList<>();
    SessionTimeout sessionTimeout;
    FusedLocationProviderClient fusedLocationProviderClient;

    private boolean contain;
    private double currentLat,currentLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_absen_pegawai);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        sharedPreferences = getSharedPreferences("UserData",MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        id_pegawai = sharedPreferences.getString("id_pegawai","0");
        eDate = findViewById(R.id.tvDate);
        eInstansi = findViewById(R.id.tvInstansiLn);
        eTahun = findViewById(R.id.tvTahun);
        eNip = findViewById(R.id.tvNip);
        eMasuk = findViewById(R.id.tvMasukLn);
        ePulang = findViewById(R.id.tvPulangLn);
        eKet = findViewById(R.id.tvKeteranganLn);
        fabAbsen= findViewById(R.id.fabAbasen);

        getRekap();
        grantPermission();
        fabAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid()) {
//                    refreshLatLng();
                    present();
                    getRekap();
                }
            }
        });
        eInstansi.setText(sharedPreferences.getString("instansi","null"));
        Date currentTime = Calendar.getInstance().getTime();
//        eTahun.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        switch (currentTime.getDay()){
            case 1:
                hari = "Senin";
                break;
            case 2:
                hari = "Selasa";
                break;
            case 3:
                hari = "Rabu";
                break;
            case 4:
                hari = "Kamis";
                break;
            case 5:
                hari = "Jum'at";
                break;
            case 6:
                hari = "Sabtu";
                break;
            case 7:
                hari = "Minggu";
                break;
            default:
                hari = ". . . ";
                break;
        }
        switch (currentTime.getMonth()){
            case 0:
                bulan = "Januari";
                break;
            case 1:
                bulan = "Februari";
                break;
            case 2:
                bulan = "Maret";
                break;
            case 3:
                bulan = "April";
                break;
            case 4:
                bulan = "Mei";
                break;
            case 5:
                bulan = "Juni";
                break;
            case 6:
                bulan = "Juli";
                break;
            case 7:
                bulan = "Agustus";
                break;
            case 8:
                bulan = "September";
                break;
            case 9:
                bulan = "Oktober";
                break;
            case 10:
                bulan = "November";
                break;
            case 11:
                bulan = "Desember";
                break;

            default:
                break;
        }
        eDate.setText(hari+", "+ String.valueOf(currentTime.getDate()) + " " + bulan);
        //buat huruf awal jadi besar



        String dateCombination = eDate.getText().toString();
        SpannableString ssDate = new SpannableString(dateCombination);
        ssDate.setSpan( new RelativeSizeSpan(2f),0,1,0);
        eDate.setText(ssDate);

        if(mList != null && !mList.isEmpty()){
            contain = PolyUtil.containsLocation(currentLat,currentLong,mList,true);
        }


        //buat user session time out
        sessionTimeout = new SessionTimeout();

        sessionTimeout.startUserSession();

        sessionTimeout.setSessionLogout(this);
    }

    private void present() {
        Call<JsonRespon> call;

        //parameternnya belum
        SharedPreferences sharedUserData = getSharedPreferences("UserData", MODE_PRIVATE);
        String id_pegawai = sharedUserData.getString("id_pegawai", null);
        call = Api.getClient().absen(id_pegawai, currentLat, currentLong,getSharedPreferences("UserData",MODE_PRIVATE).getString("imei_code",null));

        call.enqueue(new Callback<JsonRespon>() {
            @Override
            public void onResponse(Call<JsonRespon> call, Response<JsonRespon> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Is Successfull");
                    if (response.body().getCode()) {
                        Toast.makeText(LandingAbsenPegawaiActivity.this, "Berhasil absen", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LandingAbsenPegawaiActivity.this, " ", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonRespon> call, Throwable t) {
                Toast.makeText(LandingAbsenPegawaiActivity.this, " error : " + t, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //do nothings when back pressed
    }

    private void getRekap() {
        Call<JasonRekap> call;

        call = Api.getClient().getRekap(id_pegawai);
        Log.d("absenPegawai", "getRekap: id " + id_pegawai);

        call.enqueue(new Callback<JasonRekap>() {
            @Override
            public void onResponse(Call<JasonRekap> call, Response<JasonRekap> response) {
                if(response.isSuccessful()){
                    eNip.setText("NIP :"+response.body().getData().getNip());
                    Log.d(TAG, "onResponse: "+ response.body().getData().getNip());
                    eMasuk.setText(": "+response.body().getData().getMasuk());
                    ePulang.setText(": "+response.body().getData().getPulang());
                    eKet.setText(response.body().getData().getKeter());
                }

            }

            @Override
            public void onFailure(Call<JasonRekap> call, Throwable t) {
                Toast.makeText(LandingAbsenPegawaiActivity.this, " error : " + t, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    private boolean valid() {
        getMarkerPolygon();
        refreshLatLng();

        if(!contain){
            Log.d(TAG, "valid: is not");
            Toast.makeText(this, "Diluar Area Absen!", Toast.LENGTH_SHORT).show();
            return false;

        }
        return true;
    }

    private void grantPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted
                        refreshLatLng();
                        contain = PolyUtil.containsLocation(currentLat,currentLong,mList,true);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            Toast.makeText(LandingAbsenPegawaiActivity.this, "Permission Location Denied", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void refreshLatLng() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
//                    googleMap.setMyLocationEnabled(false);
                    if (!location.isFromMockProvider()) {
                        currentLat = location.getLatitude();
                        currentLong = location.getLongitude();
//                        googleMap.setMyLocationEnabled(true);

                    } else {
                        warnShownUp();
                    }
                    Log.d(TAG, "currentLocation: " + currentLat + " : " + currentLong + "isMOCK ? " + location.isFromMockProvider());

                } else {
                    Toast.makeText(LandingAbsenPegawaiActivity.this, "No location found", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void warnShownUp() {
        final AlertDialog.Builder fakeGPS = new AlertDialog.Builder(LandingAbsenPegawaiActivity.this);
        fakeGPS.setMessage("Make Sure to turned off the FAKE GPS!");
        fakeGPS.setCancelable(false);
        fakeGPS.setIcon(R.drawable.ic_baseline_gps_off_24);
        fakeGPS.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refreshLatLng();
            }
        });
        fakeGPS.create();
        fakeGPS.show();
    }
    private void getMarkerPolygon() {
        Call<JsonMarker> call;

        call = Api.getClient().present();

        call.enqueue(new Callback<JsonMarker>() {
            @Override
            public void onResponse(Call<JsonMarker> call, Response<JsonMarker> response) {
                if (response.isSuccessful()) {
//                    Log.d(TAG, "onResponse: "+ response.body().getData());

                    List<PolygonMarker> polygons = response.body().getData();

                    for (PolygonMarker marker : polygons) {
                        LatLng position = new LatLng(Double.parseDouble(marker.getLat()), Double.parseDouble(marker.getLng()));
                        mList.add(position);
//                        Log.d(TAG, "onResponse: lat -> " + marker.getLat() + " && lng -> "+ marker.getLng());
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<JsonMarker> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }


    @Override
    public void onSessionLogout() {
        finish();


        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("Username",null) != null){
            sharedPreferences.edit().clear().commit();
            Intent intent = new Intent(LandingAbsenPegawaiActivity.this,SplashScreen.class);
            startActivity(intent);
        }
    }
}