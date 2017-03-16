package com.example.android.location.core;

/**
 * Created by heqingbao on 2017/3/15.
 */
public enum LocationType {

    AMAP("高德定位"),
    BAIDU("百度定位"),
    TENCENT("腾讯定位");

    String desc;

    LocationType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
