package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.HeWeather;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.httpUtil.HttpUtil;
import com.example.administrator.justfortest2.util.StatusBarUtil;
import com.example.administrator.justfortest2.util.httpUtil.RetrofitHttpUtil;
import com.example.administrator.justfortest2.util.httpUtil.Utility;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class Tabs extends AppCompatActivity {

    final static String TAG = "zff";

    public TextView textView;

    public ImageView bingPicImg;

    public DrawerLayout drawerLayout;

    public LinearLayout ll;

    private LocalBroadcastManager localBroadcastManager;

    public SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;

    public List<WeatherFragment> mFragments = new ArrayList<>();

    public Weather weather;

    public HourlyAndDaily hourlyAndDaily;

    public String mWeatherId;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    List<FavouriteCity> savedList;

    Button preSelectedBtn;

    ProgressDialog progressDialog;

    private int position = -1;


    /**
     * 构造按钮
     */
    public void initBtn(Button btn) {
        btn.setLayoutParams(new ViewGroup.LayoutParams(15, 15));
        btn.setBackgroundResource(R.drawable.dot);
        ll.addView(btn);
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getContext())
                        .edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(Tabs.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 更新天气
     */
    public void refresh(final String weatherId) {

        final int currentItem = mViewPager.getCurrentItem();

        /*final String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";*/

        RetrofitHttpUtil.getHeWeather("https://free-api.heweather.com/",
                "cn101010400",
                "8c5ef408aec747eb956be39c65689b5f",
                new retrofit2.Callback<HeWeather>() {

                    Intent intent = new Intent("com.example.administrator.justfortest2.STOP_REFRESH");

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
                                        final HourlyAndDaily hourlyAndDaily = response.body();
                                        if (weather != null && "ok".equals(weather.status) &&
                                                hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                                            FavouriteCity favouriteCity = new FavouriteCity();
                                            favouriteCity.setWeather(new Gson().toJson(weather));
                                            favouriteCity.setCaiweather(new Gson().toJson(hourlyAndDaily));
                                            favouriteCity.updateAll("weatherId = ?", weatherId);
                                            WeatherFragment fragment = WeatherFragment.newInstance(new Gson().toJson(weather), new Gson().toJson(hourlyAndDaily));
                                            mFragments.set(currentItem, fragment);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    initView();
                                                    mViewPager.setCurrentItem(currentItem);
                                                }
                                            });
                                        } else {
                                            Log.d("MyFault", "onResponse: false");
                                            Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();

                                        }
                                        localBroadcastManager.sendBroadcast(intent);

                                    }
                                    @Override
                                    public void onFailure(retrofit2.Call<HourlyAndDaily> call, Throwable t) {
                                        Log.d(TAG, "onResponse2: " + t.getMessage());
                                        Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();
                                        localBroadcastManager.sendBroadcast(intent);
                                    }
                                });
                    }
                    @Override
                    public void onFailure(retrofit2.Call<HeWeather> call, Throwable t) {
                        Log.d(TAG, "onFailure1: " + t.getMessage());
                        Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();
                        localBroadcastManager.sendBroadcast(intent);
                    }
                });
        loadBingPic();
    }

    /**
     *根据侧滑菜单传来的数据请求天气
     */
    public void setWeatherOnPosition0(final String weatherId) {

        savedList = DataSupport.findAll(FavouriteCity.class);

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
                                    public void onResponse(retrofit2.Call<HourlyAndDaily> call, retrofit2.Response<HourlyAndDaily> response)  {

                                        final HourlyAndDaily hourlyAndDaily = response.body();
                                        if (weather != null && "ok".equals(weather.status) &&
                                                hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                                            FavouriteCity favouriteCity = new FavouriteCity();
                                            favouriteCity.setWeather(new Gson().toJson(weather));
                                            favouriteCity.setWeatherId(weatherId);
                                            favouriteCity.setCaiweather(new Gson().toJson(hourlyAndDaily));
                                            favouriteCity.setName(weather.basic.cityName);
                                            favouriteCity.updateAll("id = ?", "1");
                                            mWeatherId = weather.basic.weatherId;
                                            final WeatherFragment fragment = WeatherFragment.newInstance(new Gson().toJson(weather), new Gson().toJson(hourlyAndDaily));
                                            mFragments.set(0, fragment);
                                            savedList.get(0).setName(weather.basic.cityName);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    initView();
                                                    if (preSelectedBtn != null) {
                                                        preSelectedBtn.setBackgroundResource(R.drawable.dot);
                                                        Button currentBtn = (Button) ll.getChildAt(0);
                                                        currentBtn.setBackgroundResource(R.drawable.selected_dot);
                                                        preSelectedBtn = currentBtn;
                                                    }
                                                    textView.setText(weather.basic.cityName);
                                                    closeProgressDialog();
                                                }
                                            });
                                        } else {
                                            Log.d("MyFault", "setWeatherOnPosition0解析彩云失败");
                                            Log.d("MyFault", hourlyAndDaily.status);
                                            Log.d("MyFault", weather.status);
                                        }
                                    }
                                    @Override
                                    public void onFailure(retrofit2.Call<HourlyAndDaily> call, Throwable t) {
                                        Log.d(TAG, "onResponse2: " + t.getMessage());
                                    }
                                });
                    }
                    @Override
                    public void onFailure(retrofit2.Call<HeWeather> call, Throwable t) {
                        Log.d(TAG, "onFailure1: " + t.getMessage());
                    }
                });

    }

    /**
     * 构造页面
     */
    public void initView() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        savedList = DataSupport.findAll(FavouriteCity.class);
        if (savedList.size() != 0 && savedList.size() != mFragments.size()) {
            preSelectedBtn = new Button(this);
            mFragments.clear();
            ll.removeAllViews();
            for (int i = 0; i < savedList.size(); i++) {
                String weatherInfo = savedList.get(i).getWeather();
                String caiWeatherInfo = savedList.get(i).getCaiweather();
                WeatherFragment fragment = WeatherFragment.newInstance(weatherInfo, caiWeatherInfo);
                mFragments.add(fragment);
                initBtn(new Button(this));
            }
            initView();
            Button firstBtn = (Button) ll.getChildAt(0);
            firstBtn.setBackgroundResource(R.drawable.selected_dot);
            if (mFragments.size() == 1) {
                firstBtn.setVisibility(View.GONE);
            } else {
                firstBtn.setVisibility(View.VISIBLE);
            }
            preSelectedBtn = firstBtn;
            textView.setText(savedList.get(0).getName());
        }

        if (position != -1) {
            mViewPager.setCurrentItem(position);
            preSelectedBtn.setBackgroundResource(R.drawable.dot);
            Button currentBtn = (Button) ll.getChildAt(position);
            currentBtn.setBackgroundResource(R.drawable.selected_dot);
            preSelectedBtn = currentBtn;
            position = -1;
        }

        if (savedList.size() == 1) {
            textView.setText(savedList.get(0).getName());
        }
        //从缓存加载图片
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    /**
     * 接受来自addcity的位置
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        position = intent.getIntExtra("position", 0);
        intent.removeExtra("position");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        statusBarImmersion();
        ll = (LinearLayout) findViewById(R.id.ll_dot);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        textView = (TextView) findViewById(R.id.title_name);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mViewPager = (ViewPager) findViewById(R.id.container);
        WeatherFragment fragment = WeatherFragment.newInstance(null, null);
        mFragments.add(fragment);

        String firstName = getIntent().getStringExtra("firstName");
        if (firstName != null) {
            textView.setText(firstName);
            getIntent().removeExtra("firstName");
        }

        //打开侧滑菜单按钮
        Button button1 = (Button) findViewById(R.id.open);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);

            }
        });

        Button button2 = (Button) findViewById(R.id.btn_manager);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Tabs.this, AddCity.class);
                startActivity(intent);
            }
        });

        initView();

        //页面切换监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (preSelectedBtn != null) {
                    preSelectedBtn.setBackgroundResource(R.drawable.dot);
                    Button currentBtn = (Button) ll.getChildAt(position);
                    currentBtn.setBackgroundResource(R.drawable.selected_dot);
                    preSelectedBtn = currentBtn;
                }
                textView.setText(savedList.get(position).getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * viewpager适配器
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        List<WeatherFragment> fragmentList;

        private SectionsPagerAdapter(FragmentManager fm, List<WeatherFragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        /**
         * @param position 当前所在页
         * @return 当前页要显示的内容
         */
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragmentList.get(position);
        }

        //设置显示的总页数
        @Override
        public int getCount() {
            // Show 3 total pages.
            return fragmentList.size();
        }

    }

    /**
     * 显示进度对话框
     */
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 设置状态栏沉浸
     */
    public void statusBarImmersion(){
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        //StatusBarUtil.setRootViewFitsSystemWindows(this,true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }
    }


}
