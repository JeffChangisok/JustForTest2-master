package com.example.administrator.justfortest2.util.httpUtil;

import com.example.administrator.justfortest2.gson.HeWeather;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.httpUtil.WeatherRequest;

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHttpUtil {
    /**
     *
     * @param baseUrl   baseUrl
     * @param city      城市ID  例：cn101010400
     * @param key       和风天气账号的key
     * @param callback  请求回调
     */
    public static void getHeWeather(String baseUrl, String city, String key, Callback<HeWeather> callback){
        initRetrofit(baseUrl).getHeWeather(city, key).enqueue(callback);
    }

    /**
     *
     * @param baseUrl   baseUrl
     * @param jing      经度，lon
     * @param wei       纬度，lat
     * @param callback  请求回调
     */
    public static void getCaiWeather(String baseUrl, String jing, String wei, Callback<HourlyAndDaily> callback){
        initRetrofit(baseUrl).getCaiWeather(jing, wei).enqueue(callback);
    }

    private static WeatherRequest initRetrofit(String baseUrl){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(WeatherRequest.class);
    }
}
