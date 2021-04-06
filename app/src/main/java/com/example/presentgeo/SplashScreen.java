package com.example.presentgeo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Base64;

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIME = 3000; //This is 3 seconds
    private String TAG = "SplashScreen";
    String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        grantPhonePermission();
        imei = getUniqueIMEIId(SplashScreen.this);

        savetoShared();
//        Toast.makeText(this, "imei" + imei, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FusedLocationProviderClient fusedLocationProviderClient;
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SplashScreen.this);

                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        SharedPreferences.Editor edit = sharedPreferences.edit();

                        Log.d(TAG, "onSuccess: " + location.getLatitude() + location.getLongitude()+ location.getAccuracy());
                        edit.putString("latitude", String.valueOf(location.getLatitude()));
                        edit.putString("longitude", String.valueOf(location.getLongitude()));
                        edit.commit();
                    }
                });
                if(!sharedPreferences.getBoolean("isAdmin",false)){
                    startActivity(new Intent(SplashScreen.this, LandingAbsenPegawaiActivity.class));
                }else{
                    startActivity(new Intent(SplashScreen.this,LandingActivity.class));
                }
                if(!sharedPreferences.contains("Username")){
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                }
            }
        },SPLASH_TIME);
    }

    private void grantPhonePermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_PHONE_STATE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                savetoShared();

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        });
    }

    private void savetoShared() {
        final SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        SharedPreferences.Editor put = sharedPreferences.edit();
        put.putString("imei_code",imei);
        put.commit();
    }

    public static String getUniqueIMEIId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            String imei = telephonyManager.getDeviceId();
            Log.e("imei", "=" + imei);
            if (!imei.isEmpty()) {
                Log.d("imei", "getUniqueIMEIId: " + imei);
                return imei;
            } else {
                Log.d("imei", "getUniqueIMEIId: " + android.os.Build.SERIAL);

                return android.os.Build.SERIAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "not_found";
    }
}