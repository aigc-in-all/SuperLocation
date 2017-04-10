package com.hqb.android.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hqb.android.location.core.amap.AmapLocationManager;
import com.hqb.android.location.core.baidu.BaiduLocationManager;
import com.hqb.android.location.core.tencent.TencentLocationManager;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class LocationManager {

    private static LocationManager instance;

    private Context appContext;

    public static LocationManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new LocationManager(context);
        }
        return instance;
    }

    private LocationManager(@NonNull Context context) {
        appContext = context.getApplicationContext();
    }

    /**
     * 遍历获取位置（高德->腾讯->百度）
     *
     * 注：百度定位结果比较容易区分权限问题还是网络问题引起的定位失败
     */
    public void requestLocationOnceAllInOne(LocationListener listener) {
        requestLocationOnceByAmap(listener);
    }

    private void requestLocationOnceByAmap(final LocationListener listener) {
        AmapLocationManager.getInstance(appContext).requestLocationOnce(new LocationListener() {
            @Override
            public void onReceiveLocation(@NonNull LocationType locationType, @NonNull Location location) {
                listener.onReceiveLocation(locationType, location);
            }

            @Override
            public void onError(@NonNull LocationType locationType, @NonNull LocationErrorType errorType, @Nullable String reason) {
                requestLocationOnceByTencent(listener);
            }
        });
    }

    private void requestLocationOnceByTencent(final LocationListener listener) {
        TencentLocationManager.getInstance(appContext).requestLocationOnce(new LocationListener() {
            @Override
            public void onReceiveLocation(@NonNull LocationType locationType, @NonNull Location location) {
                listener.onReceiveLocation(locationType, location);
            }

            @Override
            public void onError(@NonNull LocationType locationType, @NonNull LocationErrorType errorType, @Nullable String reason) {
                requestLocationOnceByBaidu(listener);
            }
        });
    }

    private void requestLocationOnceByBaidu(final LocationListener listener) {
        BaiduLocationManager.getInstance(appContext).requestLocationOnce(new LocationListener() {
            @Override
            public void onReceiveLocation(@NonNull LocationType locationType, @NonNull Location location) {
                listener.onReceiveLocation(locationType, location);
            }

            @Override
            public void onError(@NonNull LocationType locationType, @NonNull LocationErrorType errorType, @Nullable String reason) {
                listener.onError(locationType, errorType, reason);
            }
        });
    }


}
