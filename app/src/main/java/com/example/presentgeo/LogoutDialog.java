package com.example.presentgeo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

public class LogoutDialog {
    private Activity activity;
    private AlertDialog dialog;

    public LogoutDialog(Activity activity) {
        this.activity = activity;
    }
    public void starDialog(View view, final MaterialButton logout){
        AlertDialog.Builder  builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_logout, (ViewGroup) view.getRootView(),false);
        builder.setView(v);
        builder.setCancelable(false);
        dialog = builder.create();
        MaterialButton dPositive = v.findViewById(R.id.btnPositive);
        MaterialButton dNegative = v.findViewById(R.id.btnNegative);

        dPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = activity.getSharedPreferences("UserData", Context.MODE_PRIVATE);
                if(sharedPreferences.getString("Username",null) != null){
                    sharedPreferences.edit().clear().commit();
                    activity.startActivity(new Intent(activity,SplashScreen.class));
                }
            }
        });
        dNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                logout.setClickable(true);

            }
        });
        dialog.show();

    }
    public void dismissDialog(){
        dialog.dismiss();
    }
}
