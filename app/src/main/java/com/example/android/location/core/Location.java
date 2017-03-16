package com.example.android.location.core;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class Location {

    private double longitude;
    private double latitude;
    private String address;
    private long time; // 当前位置的生成时间

    public Location(double longitude, double latitude, String address) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Location{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", address='" + address + '\'' +
                ", time=" + time +
                '}';
    }
}
