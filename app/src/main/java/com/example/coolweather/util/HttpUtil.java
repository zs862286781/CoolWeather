package com.example.coolweather.util;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//http://guolin.tech/api/china
//        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
//http://guolin.tech/api/bing_pic
public class HttpUtil {
    private static HttpUtil httpUtil;
    private static OkHttpClient httpClient;
    public static HttpUtil getInstance() {
        if (httpUtil == null) {
            httpUtil = new HttpUtil();
        }
        return httpUtil;
    }
    public HttpUtil() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
        }
    }
    public interface HttpListener {
        void onSuccess(String response);
        void onFailure(int code);
    }
    public void sendRequest(String url, HttpListener listener) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(-1);
                Log.d("onFailure",e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("onResponse",response.message());
                if (response.code() == 200) {
                    listener.onSuccess(response.body().string());
                }else {
                    listener.onFailure(response.code());
                }
            }
        });
    }
}
