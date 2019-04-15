package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.HeWeather;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.httpUtil.RetrofitHttpUtil;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCity extends AppCompatActivity {

    private static final String TAG = "zhangfan";

    EditText eSearch;
    ImageView ivDeleteText;
    ListView mListView;
    ArrayList<Map<String, Object>> mData = new ArrayList();
    List<String> mListTitle = new ArrayList();
    List<String> mListText = new ArrayList();
    List<String> mListId = new ArrayList();
    Weather weather;
    SimpleAdapter adapter;
    Handler myhandler = new Handler();

    Runnable eChanged = new Runnable() {
        public void run() {
            String data = eSearch.getText().toString();
            mData.clear();
            getmDataSub(mData, data);
            adapter.notifyDataSetChanged();
        }
    };

    public SearchCity() {
    }

    private ProgressDialog progressDialog;

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
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
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "进入SearchActivity");
        setContentView(R.layout.activity_search_city);
        mListView = (ListView) findViewById(R.id.mListView);
        eSearch = (EditText) findViewById(R.id.etSearch);
        set_eSearch_TextChanged();
        set_ivDeleteText_OnClick();
        set_mListView_adapter();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String weatherId = mData.get(position).get("id").toString();
                final String cityName = mData.get(position).get("title").toString();

                List<FavouriteCity> list = LitePal.where("name = ?", cityName)
                        .find(FavouriteCity.class);

                if (list.isEmpty()) {
                    showProgressDialog();
                    Log.d(TAG, "onItemClick,weatherId = " + weatherId);
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
                                                    Log.d(TAG, "onResponse2: 彩云请求成功");
                                                    final HourlyAndDaily hourlyAndDaily = response.body();
                                                    if (weather != null && "ok".equals(weather.status) &&
                                                            hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                                                        FavouriteCity favouriteCity = new FavouriteCity();
                                                        favouriteCity.setName(cityName);
                                                        favouriteCity.setWeather(new Gson().toJson(weather));
                                                        favouriteCity.setWeatherId(weatherId);
                                                        favouriteCity.setCaiweather(new Gson().toJson(hourlyAndDaily));
                                                        favouriteCity.save();
                                                        Log.d(TAG, "onResponse2: 天气保存成功");
                                                        closeProgressDialog();
                                                        finish();
                                                    } else {
                                                        Log.d(TAG, "onResponse2: 天气信息处理失败");
                                                        closeProgressDialog();
                                                    }

                                                }
                                                @Override
                                                public void onFailure(retrofit2.Call<HourlyAndDaily> call, Throwable t) {
                                                    Log.d(TAG, "onFailure2: 彩云天气请求失败" + t.getMessage());
                                                }
                                            });
                                }
                                @Override
                                public void onFailure(retrofit2.Call<HeWeather> call, Throwable t) {
                                    Log.d(TAG, "onFailure1: 和风天气请求失败" + t.getMessage());
                                }
                            });

                    Intent intent = new Intent(SearchCity.this, AddCity.class);
                    intent.putExtra("cityName", mData.get(position).get("title").toString());
                    setResult(RESULT_OK, intent);

                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SearchCity.this);
                    dialog.setTitle("这是一个意外(°ー°〃)");
                    dialog.setMessage("请检查你是否已经添加过该城市了");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("好吧", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    /**
     * 设置适配器
     */
    private void set_mListView_adapter() {
        mListView.setVisibility(View.GONE);
        getmData(mData);
        adapter = new SimpleAdapter(this, mData, android.R.layout.simple_list_item_2,
                new String[]{"title", "text", "id"},
                new int[]{android.R.id.text1, android.R.id.text2});
        mListView.setAdapter(adapter);
    }

    /**
     * 编辑框文字改变时的操作
     */
    private void set_eSearch_TextChanged() {

        eSearch.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    ivDeleteText.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                } else {
                    mListView.setVisibility(View.VISIBLE);
                    ivDeleteText.setVisibility(View.VISIBLE);
                }
                myhandler.post(eChanged);
            }
        });
    }

    /**
     * 根据编辑框的文字检索城市信息并放到数据集中
     * @param mDataSubs 用于显示在listview中的数据
     * @param data 编辑框中的文字
     */
    private void getmDataSub(ArrayList<Map<String, Object>> mDataSubs, String data) {
        int length = mListTitle.size();

        for (int i = 0; i < length; ++i) {
            if (mListTitle.get(i).contains(data) || mListText.get(i).contains(data)) {
                HashMap item = new HashMap();
                item.put("title", mListTitle.get(i));
                item.put("text", mListText.get(i));
                item.put("id", mListId.get(i));
                mDataSubs.add(item);
            }
        }

    }

    /**
     * 删除按钮点击事件
     */
    private void set_ivDeleteText_OnClick() {
        ivDeleteText = (ImageView) findViewById(R.id.ivDeleteText);
        ivDeleteText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                eSearch.setText("");
            }
        });
    }

    /**
     * 读取文本文件内容并存入list中
     * @param files
     * @param list
     */
    private void putData(String files[], List<String> list) {
        boolean flag = false;
        if ("cityid_1.txt".equals(files[0])) {
            flag = true;
        }
        try {
            for (int i = 0; i < 8; i++) {
                InputStreamReader inputReader = new InputStreamReader(getAssets().open(files[i]));
                BufferedReader bufReader = new BufferedReader(inputReader);
                String line = "";
                if (flag) {
                    while ((line = bufReader.readLine()) != null) {
                        list.add("CN" + line);
                    }
                } else {
                    while ((line = bufReader.readLine()) != null) {
                        list.add(line);
                    }
                }

                inputReader.close();
                bufReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     *
     * @param mDatas
     */
    private void getmData(ArrayList<Map<String, Object>> mDatas) {
        HashMap item = new HashMap();
        String cityIdFiles[] = {"cityid_1.txt", "cityid_2.txt", "cityid_3.txt", "cityid_4.txt",
                "cityid_5.txt", "cityid_6.txt", "cityid_7.txt", "cityid_8.txt"};
        String titleFiles[] = {"title_1.txt", "title_2.txt", "title_3.txt", "title_4.txt",
                "title_5.txt", "title_6.txt", "title_7.txt", "title_8.txt"};
        String textFiles[] = {"text_1.txt", "text_2.txt", "text_3.txt", "text_4.txt",
                "text_5.txt", "text_6.txt", "text_7.txt", "text_8.txt"};

        putData(cityIdFiles, mListId);
        putData(titleFiles, mListTitle);
        putData(textFiles, mListText);

        for (int i = 0; i < mListId.size(); i++) {
            item.put("id", mListId.get(i));
            item.put("title", mListTitle.get(i));
            item.put("text", mListText.get(i));
            mDatas.add(item);
        }
    }

}
