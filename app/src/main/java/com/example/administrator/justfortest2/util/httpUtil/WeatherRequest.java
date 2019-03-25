package com.example.administrator.justfortest2.util.httpUtil;

import com.example.administrator.justfortest2.gson.HeWeather;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherRequest {

    /*
    https://free-api.heweather.com/v5/weather?city=cn101010400&key=8c5ef408aec747eb956be39c65689b5f
     */
    @GET("v5/weather")
    Call<HeWeather> getHeWeather (@Query("city") String city, @Query("key") String key);

    /*
    https://api.caiyunapp.com/v2/D99AfEnT96xj1fsy/116.65352631,40.12893677/forecast.json
     */
    @GET("v2/D99AfEnT96xj1fsy/{jing},{wei}/forecast.json")
    Call<HourlyAndDaily> getCaiWeather(@Path("jing") String jing, @Path("wei") String wei);

}
