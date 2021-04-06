package com.example.presentgeo.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SessionTimeout {
    Timer timer;
    private long delay = 15 * 60 * 1000; // 15 mins of login will be logout
    private LogoutSessionTimeOut listener;

    public SessionTimeout() {

    }

    public void startUserSession(){
        cancelTimer();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                listener.onSessionLogout();
            }
        },delay);
    }

    private void cancelTimer() {
        if(timer!= null) timer.cancel();
    }

    public void setSessionLogout(LogoutSessionTimeOut listener){
        this.listener = listener;
    }

    public void cancelTimerSession() {
        startUserSession();
    }
}
