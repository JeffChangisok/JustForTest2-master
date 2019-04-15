package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.administrator.justfortest2.bean.HourlyInfo;
import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.DailyTemp;
import com.example.administrator.justfortest2.gson.HeWeather;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.HourlyTemp;
import com.example.administrator.justfortest2.gson.Result;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.DateUtils;
import com.example.administrator.justfortest2.adapter.HourlyAdapter;
import com.example.administrator.justfortest2.util.httpUtil.RetrofitHttpUtil;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/5/2.
 */

public class WeatherFragment extends Fragment {

    final static String TAG = "zhangfan";
    private List<HourlyInfo> hourlyList = new ArrayList<>();
    private ScrollView weatherLayout;

    public SwipeRefreshLayout swipeRefresh;
    public String mWeatherId;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;

    private TextView updateTime;
    private TextView degreeText;
    private TextView weatherInfoText;

    private RecyclerView hourlyRecyclerView;
    private TextView hourlyTemp;
    private ImageView hourlyImage;
    private TextView hourlyDate;

    private LinearLayout forecastLayout;

    private TextView aqi;
    private TextView pm25;
    private TextView qlty;
    //private TextView airInfo;

    private TextView comfortTitle;
    private TextView comfortInfo;
    private TextView clothTitle;
    private TextView clothInfo;
    private TextView travelTitle;
    private TextView travelInfo;
    private TextView sportTitle;
    private TextView sportInfo;

    public Weather weather;
    public HourlyAndDaily hourlyAndDaily;


    public WeatherFragment() {
    }


    /*
        ViewPager里面是一个个fragment，每次创建一个个fragment的时候，通过fragment的newInstance方法
        将天气信息保存进去，在需要显示的时候，直接提取信息即可。需要更新天气时，提取当前子项的城市CN代码
        通过CN代码再去请求天气信息，将结果保存在一个新的fragment中，然后覆盖原来位置上的fragment。
        WeatherFragment fragment = WeatherFragment.newInstance(new Gson().toJson(weather), new Gson().toJson(hourlyAndDaily));
         mFragments.set(currentItem, fragment);
         */
    public static WeatherFragment newInstance(String arg1, String arg2) {
        if (arg1.equals("")){
            Log.d(TAG, "WeatherFragment:newInstance参数为空");
        } else {
            Log.d(TAG, "WeatherFragment:newInstance参数非空");
        }
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key1", arg1);
        bundle.putString("key2", arg2);
        fragment.setArguments(bundle);
        return fragment;
    }

    String selectedWeather;
    String selectedCaiWeather;

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "WeatherFragment:onCreateView");
        View view = inflater.inflate(R.layout.activity_weather, null);
        weatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        updateTime = (TextView) view.findViewById(R.id.update_time);
        degreeText = (TextView) view.findViewById(R.id.degree_text);
        weatherInfoText = (TextView) view.findViewById(R.id.weather_info_text);

        hourlyRecyclerView = (RecyclerView) view.findViewById(R.id.hourly_recycler_view);
        hourlyTemp = (TextView) view.findViewById(R.id.hourly_temp);
        hourlyImage = (ImageView) view.findViewById(R.id.hourly_image);
        hourlyDate = (TextView) view.findViewById(R.id.hourly_date);

        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);

        aqi = (TextView) view.findViewById(R.id.aqi);
        pm25 = (TextView) view.findViewById(R.id.pm25);
        qlty = (TextView) view.findViewById(R.id.qlty);
        //airInfo = (TextView) view.findViewById(R.id.air_info);

        comfortTitle = (TextView) view.findViewById(R.id.comfort_title);
        comfortInfo = (TextView) view.findViewById(R.id.comfort_info);
        clothTitle = (TextView) view.findViewById(R.id.cloth_title);
        clothInfo = (TextView) view.findViewById(R.id.cloth_info);
        travelTitle = (TextView) view.findViewById(R.id.travel_title);
        travelInfo = (TextView) view.findViewById(R.id.travel_info);
        sportTitle = (TextView) view.findViewById(R.id.sport_title);
        sportInfo = (TextView) view.findViewById(R.id.sport_info);
        return view;
    }

    /*
     * 广播接收器
     * 关闭刷新小按钮
     */
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "WeatherFragment:onCreate");
        //LocalBroadcastManager是单例模式，会在Tabs活动中调用sendBroadcast(intent)方法来唤起onReceive方法
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.administrator.justfortest2.STOP_REFRESH");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "WeatherFragment:onActivityCreated");
        selectedWeather = getArguments().getString("key1");
        selectedCaiWeather = getArguments().getString("key2");
        if (selectedWeather == null) {
            Log.d(TAG, "onActivityCreated:selectedWeather == null");
            //数据库里面是否有城市
            List<FavouriteCity> cities = LitePal.findAll(FavouriteCity.class);
            if (!cities.isEmpty()) {
                Log.d(TAG, "onActivityCreated:收藏的城市非空");
                //有缓存时直接解析天气数据
                Weather weather = new Gson().fromJson(cities.get(0).getWeather(),Weather.class);
                HourlyAndDaily hourlyAndDaily =new Gson().fromJson(cities.get(0).getCaiweather(),HourlyAndDaily.class);
                mWeatherId = weather.basic.weatherId;
                showWeatherInfo(weather, hourlyAndDaily);
            } else {
                Log.d(TAG, "onActivityCreated:收藏的城市为空");
                //无缓存时去服务器查询天气8
                mWeatherId = getActivity().getIntent().getStringExtra("weather_id");
                weatherLayout.setVisibility(View.INVISIBLE);//避免当还未加载完数据时，控件显示空内容的情况
                requestWeather(mWeatherId);
            }

        } else {
            Log.d(TAG, "onActivityCreated:selectedWeather ！= null");
            Weather weather = new Gson().fromJson(selectedWeather,Weather.class);
            HourlyAndDaily hourlyAndDaily = new Gson().fromJson(selectedCaiWeather,HourlyAndDaily.class);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather, hourlyAndDaily);

        }


        // 注册下拉刷新事件
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Tabs activity = (Tabs) getActivity();
                activity.refresh(mWeatherId);
            }
        });

    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        //在utility中的解析方法中会根据返回数据的键进行处理*/
        showProgressDialog();
        Log.d(TAG, "WeatherFragment:requestWeather,weatherId = " + weatherId);
        RetrofitHttpUtil.getHeWeather("https://free-api.heweather.com/",
                weatherId,
                "8c5ef408aec747eb956be39c65689b5f",
                new retrofit2.Callback<HeWeather>() {
                    @Override
                    public void onResponse(retrofit2.Call<HeWeather> call, retrofit2.Response<HeWeather> response) {
                        Log.d(TAG, "onResponse1: 和风请求成功");
                        final Weather weather = response.body().HeWeather5.get(0);
                        RetrofitHttpUtil.getCaiWeather("https://api.caiyunapp.com/",
                                response.body().HeWeather5.get(0).basic.jing,
                                response.body().HeWeather5.get(0).basic.wei,
                                new retrofit2.Callback<HourlyAndDaily>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<HourlyAndDaily> call, retrofit2.Response<HourlyAndDaily> response) {

                                        Log.d(TAG, "onResponse2: 彩云请求成功" );
                                        final HourlyAndDaily hourlyAndDaily = response.body();

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (weather != null && "ok".equals(weather.status) &&
                                                        hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                                                    //使用数据库保存天气信息
                                                    FavouriteCity favouriteCity = new FavouriteCity();
                                                    favouriteCity.setCaiweather(new Gson().toJson(hourlyAndDaily));
                                                    favouriteCity.setWeather(new Gson().toJson(weather));
                                                    favouriteCity.setWeatherId(weatherId);
                                                    favouriteCity.setName(weather.basic.cityName);
                                                    favouriteCity.save();
                                                    mWeatherId = weather.basic.weatherId;
                                                    Log.d(TAG, "onResponse2: 保存天气成功");
                                                    showWeatherInfo(weather, hourlyAndDaily);
                                                    closeProgressDialog();
                                                } else {
                                                    Log.d(TAG, "onResponse2: 天气信息处理失败 ");
                                                }
                                            }
                                        });

                                    }
                                    @Override
                                    public void onFailure(retrofit2.Call<HourlyAndDaily> call, Throwable t) {
                                        Log.d(TAG, "onResponse2: 彩云天气请求失败" + t.getMessage());
                                    }
                                });
                    }
                    @Override
                    public void onFailure(retrofit2.Call<HeWeather> call, Throwable t) {
                        Log.d(TAG, "onFailure1: 和风天气请求失败" + t.getMessage());
                    }
                });

    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    public void showWeatherInfo(Weather weather, HourlyAndDaily hourlyAndDaily) {
        Log.d(TAG, "WeatherFragment: showWeatherInfo");
        String[] strings = weather.basic.update.updateTime.split("-| ");
        StringBuilder ss = new StringBuilder();
        ss.append(strings[1]).append("月")
                .append(strings[2]).append("日").append(strings[3]).append("发布");
        updateTime.setText(ss.toString());
        String degree = weather.now.temperature + "°";
        degreeText.setText(degree);
        weatherInfoText.setText(weather.now.more.info);

        String str1;
        String str2;
        int imageId;
        List<HourlyTemp> hourlyTempList = hourlyAndDaily.result.hourly.temperature;
        List<Result.Skycon> hourlySkyconList = hourlyAndDaily.result.hourly.skycon;
        for (int i = 0; i < hourlyTempList.size(); i++) {
            str1 = String.valueOf((int)(hourlyTempList.get(i).value+0.5)) + "℃";
            str2 = hourlyTempList.get(i).datetime.split(" ")[1];
            switch (hourlySkyconList.get(i).value) {
                case "CLEAR_DAY":
                    imageId = R.drawable.sun;
                    break;
                case "CLEAR_NIGHT":
                    imageId = R.drawable.sun;
                    break;
                case "PARTLY_CLOUDY_DAY":
                    imageId = R.drawable.partly_clody;
                    break;
                case "PARTLY_CLOUDY_NIGHT":
                    imageId = R.drawable.partly_clody;
                    break;
                case "CLOUDY":
                    imageId = R.drawable.cloudy;
                    break;
                case "RAIN":
                    imageId = R.drawable.rain;
                    break;
                case "SNOW":
                    imageId = R.drawable.snow;
                    break;
                case "WIND":
                    imageId = R.drawable.wind;
                    break;
                case "FOG":
                    imageId = R.drawable.fog;
                    break;
                case "HAZE":
                    imageId = R.drawable.haze;
                    break;
                case "SLEET":
                    imageId = R.drawable.sleet;
                    break;
                default:
                    imageId = R.drawable.unknown;
            }
            HourlyInfo hourlyInfo = new HourlyInfo(str1, str2, imageId);
            hourlyList.add(hourlyInfo);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        hourlyRecyclerView.setLayoutManager(layoutManager);
        HourlyAdapter adapter = new HourlyAdapter(hourlyList);
        hourlyRecyclerView.setAdapter(adapter);

        forecastLayout.removeAllViews();
        List<DailyTemp> dailyTempList = hourlyAndDaily.result.daily.temperature;
        List<Result.Skycon> dailySkyconList = hourlyAndDaily.result.daily.skycon;
        for (int i = 0; i < dailyTempList.size(); i++) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            ImageView forecastImage = (ImageView) view.findViewById(R.id.forecast_image);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView tempText = (TextView) view.findViewById(R.id.temp_text);
            dateText.setText(DateUtils.getWeek(dailyTempList.get(i).date));
            switch (dailySkyconList.get(i).value) {
                case "CLEAR_DAY":
                    forecastImage.setImageResource(R.drawable.sun);
                    infoText.setText("晴");
                    break;
                case "CLEAR_NIGHT":
                    forecastImage.setImageResource(R.drawable.sun);
                    infoText.setText("晴");
                    break;
                case "PARTLY_CLOUDY_DAY":
                    forecastImage.setImageResource(R.drawable.partly_clody);
                    infoText.setText("多云");
                    break;
                case "PARTLY_CLOUDY_NIGHT":
                    forecastImage.setImageResource(R.drawable.partly_clody);
                    infoText.setText("多云");
                    break;
                case "CLOUDY":
                    forecastImage.setImageResource(R.drawable.cloudy);
                    infoText.setText("阴");
                    break;
                case "RAIN":
                    forecastImage.setImageResource(R.drawable.rain);
                    infoText.setText("雨");
                    break;
                case "SNOW":
                    forecastImage.setImageResource(R.drawable.snow);
                    infoText.setText("雪");
                    break;
                case "WIND":
                    forecastImage.setImageResource(R.drawable.wind);
                    infoText.setText("有风");
                    break;
                case "FOG":
                    forecastImage.setImageResource(R.drawable.fog);
                    infoText.setText("雾");
                    break;
                case "HAZE":
                    forecastImage.setImageResource(R.drawable.haze);
                    infoText.setText("霾");
                    break;
                case "SLEET":
                    forecastImage.setImageResource(R.drawable.sleet);
                    infoText.setText("雨夹雪");
                    break;
                default:
                    forecastImage.setImageResource(R.drawable.unknown);
                    infoText.setText("未知");
            }

            StringBuilder dailyDegree = new StringBuilder();
            dailyDegree.append((int)(dailyTempList.get(i).min+0.5)).append("~")
                    .append((int)(dailyTempList.get(i).max+0.5))
                    .append("℃");
            tempText.setText(dailyDegree.toString());
            forecastLayout.addView(view);
        }

        StringBuilder aqiSB = new StringBuilder();
        StringBuilder pm25SB = new StringBuilder();

        if(weather.aqi!=null){
            aqiSB.append(weather.aqi.city.aqi).append("  μg/m³");
            pm25SB.append(weather.aqi.city.pm25).append("  μg/m³");
            qlty.setText(weather.aqi.city.qlty);
        }else{
            aqiSB.append("未知");
            pm25SB.append("未知");
            qlty.setText("未知");
        }

        aqi.setText(aqiSB.toString());
        pm25.setText(pm25SB.toString());


        comfortTitle.setText(weather.suggestion.comfort.title);
        comfortInfo.setText(weather.suggestion.comfort.info);
        clothTitle.setText(weather.suggestion.cloth.title);
        clothInfo.setText(weather.suggestion.cloth.info);
        travelTitle.setText(weather.suggestion.travel.title);
        travelInfo.setText(weather.suggestion.travel.info);
        sportTitle.setText(weather.suggestion.sport.title);
        sportInfo.setText(weather.suggestion.sport.info);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private ProgressDialog progressDialog;

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


}
