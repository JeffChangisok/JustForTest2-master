package com.example.administrator.justfortest2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.administrator.justfortest2.Service.DemoPushService;
import com.example.administrator.justfortest2.db.FavouriteCity;
import com.igexin.sdk.PushManager;

import org.litepal.LitePal;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(getString(R.string.TAG), "进入mainActivity");
        PushManager.getInstance().initialize(getApplicationContext(),DemoPushService.class);
        List<FavouriteCity> cities = LitePal.findAll(FavouriteCity.class);
        if (!cities.isEmpty()) {
            Log.d(getString(R.string.TAG), "mainActivity:收藏的城市非空，启动tabsActivity");
            Intent intent = new Intent(this, Tabs.class);
            startActivity(intent);
            finish();
        }

    }
}
