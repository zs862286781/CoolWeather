package com.example.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.coolweather.MyApplication;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utulity {
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i=0;i<allProvince.length();i++) {
                    JSONObject object = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(object.getString("name"));
                    province.setProvinceCode(object.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityResponce(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCity = new JSONArray(response);
                for (int i=0;i<allCity.length();i++) {
                    JSONObject object = allCity.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(object.getInt("id"));
                    city.setCityName(object.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponce(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCity = new JSONArray(response);
                for (int i=0;i<allCity.length();i++) {
                    JSONObject object = allCity.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(object.getString("weather_id"));
                    county.setCountyName(object.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String content = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(content,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SharedPreferences getPreferences(){
        return MyApplication.getmContext().getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getPreferencesEditor(){
        return MyApplication.getmContext().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
    }
}
