package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utulity;

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateBingPic();
        updateWeather();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHout = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHout;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        String weatherString = Utulity.getPreferences().getString("weather",null);
        if (weatherString != null) {
            Weather weather = Utulity.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
            HttpUtil.getInstance().sendRequest(weatherUrl, new HttpUtil.HttpListener() {
                @Override
                public void onSuccess(String response) {
                    Weather weather = Utulity.handleWeatherResponse(response);
                    if (weather != null && weather.status.equals("ok")) {
                        SharedPreferences.Editor editor = Utulity.getPreferencesEditor();
                        editor.putString("weather",response);
                        editor.apply();
                    }
                }
                @Override
                public void onFailure(int code) {

                }
            });
        }
    }

    private void updateBingPic() {
        HttpUtil.getInstance().sendRequest("http://guolin.tech/api/bing_pic", new HttpUtil.HttpListener() {
            @Override
            public void onSuccess(String response) {
                SharedPreferences.Editor editor = Utulity.getPreferencesEditor();
                editor.putString("bing_pic",response);
                editor.apply();
            }

            @Override
            public void onFailure(int code) {

            }
        });
    }
}
