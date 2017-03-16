package com.example.android.location.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.location.core.amap.AmapLocationManager;
import com.example.android.location.core.baidu.BaiduLocationManager;
import com.example.android.location.core.tencent.TencentLocationManager;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class LocationManager {

    private static LocationManager instance;

    public static LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    private LocationManager() {
    }

    /**
     * 遍历获取位置（高德->百度->腾讯）
     */
    public void requestLocationAllInOne(LocationListener listener) {
        requestLocationOnceByAmap(listener);
    }

    private void requestLocationOnceByAmap(final LocationListener listener) {
        AmapLocationManager.getInstance().requestLocationOnce(new LocationListener() {
            @Override
            public void onReceiveLocation(@NonNull LocationType locationType, @NonNull Location location) {
                listener.onReceiveLocation(locationType, location);
            }

            @Override
            public void onError(@NonNull LocationType locationType, @Nullable LocationErrorType errorType, @Nullable String reason) {
                requestLocationOnceByBaidu(listener);
            }
        });
    }

    private void requestLocationOnceByBaidu(final LocationListener listener) {
        BaiduLocationManager.getInstance().requestLocationOnce(new LocationListener() {
            @Override
            public void onReceiveLocation(@NonNull LocationType locationType, @NonNull Location location) {
                listener.onReceiveLocation(locationType, location);
            }

            @Override
            public void onError(@NonNull LocationType locationType, @Nullable LocationErrorType errorType, @Nullable String reason) {
                requestLocationOnceByTencent(listener);
            }
        });
    }

    private void requestLocationOnceByTencent(final LocationListener listener) {
        TencentLocationManager.getInstance().requestLocationOnce(new LocationListener() {
            @Override
            public void onReceiveLocation(@NonNull LocationType locationType, @NonNull Location location) {
                listener.onReceiveLocation(locationType, location);
            }

            @Override
            public void onError(@NonNull LocationType locationType, @Nullable LocationErrorType errorType, @Nullable String reason) {
                listener.onError(locationType, errorType, reason);
            }
        });
    }
}
