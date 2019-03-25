package com.example.administrator.justfortest2.gson;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("lat")
    public String wei;

    @SerializedName("lon")
    public String jing;

    public Update update;

    public class Update implements Serializable{

        @SerializedName("loc")
        public String updateTime;

    }

}
