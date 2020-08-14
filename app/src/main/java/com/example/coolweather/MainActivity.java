package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.coolweather.util.Utulity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String weatherString = Utulity.getPreferences().getString("weather",null);
        if (weatherString != null) {
            WeatherActivity.starActivity(this,"");
            finish();
        }
    }
}
