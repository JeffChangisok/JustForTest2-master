package com.example.administrator.justfortest2.gson;

import java.io.Serializable;
import java.util.List;

public class Result {
    public Hourly hourly;
    public Daily daily;

    public class Hourly implements Serializable{
        public List<HourlyTemp> temperature;
        public List<Skycon> skycon;
    }

    public class Daily implements Serializable{
        public List<DailyTemp> temperature;
        public List<Skycon> skycon;
    }

    public class Skycon implements Serializable{
        public String value;
    }
}
