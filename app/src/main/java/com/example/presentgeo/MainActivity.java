package com.example.presentgeo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.presentgeo.model.JsonUser;
import com.example.presentgeo.model.Parameter;
import com.example.presentgeo.service.Api;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String TAG = "LoginResponses";
    private TextInputLayout usernameField,passwordField;

    private MaterialButton login;
    Intent i;
    ProgressBar eLoading;
    private String username, password;
    SharedPreferences sharedPreferences;
    private String urlLogo,namaAplikasi,instansi,pemerintah;
    ImageView logo;
    TextView ePemerintah , eInstansi;

    @Override
    protected void onResume() {
        super.onResume();
        getImeiCode();
    }

    private void getImeiCode() {
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_PHONE_STATE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//                sendImeiCode();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();

            }
        }).check();
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        usernameField = findViewById(R.id.etUsername);
        passwordField = findViewById(R.id.etPassword);

        logo = findViewById(R.id.ivLogo);
        ePemerintah = findViewById(R.id.tvNamaPemerintah);
        eInstansi = findViewById(R.id.tvInstansi);

        getParam();
        eLoading = findViewById(R.id.prLoading);
        eLoading.setVisibility(View.GONE);
        login = findViewById(R.id.btnLogin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    eLoading.setVisibility(View.VISIBLE);
                    reqLogin();
                }
            }

        });
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);


    }

    private void getParam() {
        Call<Parameter> call;
        call = Api.getClient().param();

        call.enqueue(new Callback<Parameter>() {
            @Override
            public void onResponse(Call<Parameter> call, Response<Parameter> response) {
                if(response.isSuccessful()){
                   if(response.body().getCode()){
                       List<Parameter.Param> mListParam = new ArrayList<>();
                       mListParam = response.body().getData();
                       for(Parameter.Param param : mListParam){
                           if(param.getParam().equals("pemerintah_logo")){
                               urlLogo = Api.getURL() + Api.getAsset() + param.getVal();
                               Glide.with(MainActivity.this).load(urlLogo).into(logo);
                               Log.d(TAG, "onResponse: logo aplikasi" + urlLogo);
                           }
                           if(param.getParam().equals("aplikasi")){
                               namaAplikasi = param.getVal();
                           }
                           if(param.getParam().equals("pemerintah")){
                               pemerintah = param.getVal();
                               ePemerintah.setText(pemerintah);
                           }
                           if(param.getParam().equals("instansi")){
                               instansi = param.getVal();
                               eInstansi.setText(instansi);
                           }
                           SharedPreferences.Editor putParam = sharedPreferences.edit();
                           putParam.putString("instansi",instansi);
                           putParam.putString("pemerintah",pemerintah);
                           putParam.putString("aplikasi",namaAplikasi);

                           putParam.commit();


                       }
                   }
                }
            }

            @Override
            public void onFailure(Call<Parameter> call, Throwable t) {

            }
        });
    }

    private void reqLogin() {
        //menggunakan basic auth
        Call<JsonUser> call;

        String base = username + ":" + password;
        SharedPreferences getSharedPreferences = getSharedPreferences("UserData",MODE_PRIVATE);

        String auth  = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
//        Log.d(TAG, "reqLogin: "+ getSharedPreferences.getString("imei_code","null"));
        call = Api.getClient().login(auth,getSharedPreferences.getString("imei_code","null"));

        call.enqueue(new Callback<JsonUser>() {
            @Override
            public void onResponse(Call<JsonUser> call, Response<JsonUser> response) {
                if(response.isSuccessful()){
                    if(response.body().getCode()){
                        Log.d(TAG, "onResponse: " + response.body().getMessage());
                        SharedPreferences.Editor editor =sharedPreferences.edit();
                        Log.d(TAG, "onResponse: " + username + ":" + password);
                        if(response.body().getUser() != null){
                            editor.putString("Username", response.body().getUser().getUsername());
                            editor.putString("nama", response.body().getUser().getNama());
                            editor.putString("id_pegawai", response.body().getUser().getId_pegawai());
                            editor.putString("avatar", response.body().getUser().getAvatar());
                            editor.putString("login_state", response.body().getUser().getLogin_state());
                            editor.putString("nama_role", response.body().getUser().getNama_role());
                            editor.putString("id_peg_jabatan", response.body().getUser().getId_peg_jabatan());
                            editor.putString("uraian_jabatan", response.body().getUser().getUraian_jabatan());
                            editor.putString("id_unit", response.body().getUser().getId_unit());
                            editor.putString("unit", response.body().getUser().getUnit());
                            editor.putString("id_bidang", response.body().getUser().getId_bidang());
                            editor.putString("bidang", response.body().getUser().getBidang());
                            editor.putBoolean("isAdmin", response.body().getUser().isAdmin());
                            editor.commit();

                        }
//                        sendImeiCode();
                        if(response.body().getUser().isAdmin()){
                            i = new Intent(MainActivity.this, LandingActivity.class);

                            startActivity(i);
                        }
                        if(!response.body().getUser().isAdmin()){
                            i = new Intent(MainActivity.this, LandingAbsenPegawaiActivity.class);
                            startActivity(i);
                        }

                    }else{
                        usernameField.setError("Username atau password salah!");
                    }
                    if(!response.body().getCode()){
                        String message = response.body().getMessage();
//                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        usernameField.setError(message);
                    }

                }else{
                    // error case
                    switch (response.code()) {
                        case 404:
                            Toast.makeText(MainActivity.this, "not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(MainActivity.this, "server broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "Either one of email password doesn't match", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onResponse: "+ response.code());

                            break;
                    }
                }
                eLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<JsonUser> call, Throwable t) {
                Log.d(TAG, "onFailure: " +  t );
                Toast.makeText(MainActivity.this, "Error : " + t, Toast.LENGTH_SHORT).show();
                eLoading.setVisibility(View.GONE);
            }
        });

    }

    private boolean validate() {
        username = usernameField.getEditText().getText().toString().trim();
        password = passwordField.getEditText().getText().toString();
        if(username.isEmpty()){
            usernameField.setError("Harus diisi!");
            return false;
        }
        if(password.isEmpty()){
            passwordField.setError("Harus diisi!");
            return false;
        }

        return true;

    }
}
