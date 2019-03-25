package com.example.administrator.justfortest2.gson;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature implements Serializable{

        public String max;

        public String min;

    }

    public class More implements Serializable{

        @SerializedName("txt_d")
        public String info;

    }

}
