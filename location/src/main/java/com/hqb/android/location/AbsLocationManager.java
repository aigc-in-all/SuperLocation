package com.hqb.android.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heqingbao on 2017/3/15.
 */
public abstract class AbsLocationManager {

    private static final String TAG = "LocationManager";

    protected static final int SCAN_INTERVAL = 2000;

    protected Context context;

    private List<LocationListener> listeners = new ArrayList<>();

    // 是否持续定位
    protected volatile boolean isContinuous;

    private Location cacheLocation;
    private long locationSuccessTime;

    public AbsLocationManager(Context context) {
        this.context = context;
    }

    protected void notifyLocationSuccess(@NonNull Location location) {
        if (cacheLocation != location) {
            locationSuccessTime = System.currentTimeMillis();
        }

        // cache
        cacheLocation = location;

        Log.d(TAG, "获取位置成功，开始回调("+ listeners.size() + "个Listeners)");

        for (LocationListener listener : listeners) {
            if (listener != null) {
                listener.onReceiveLocation(getLocationType(), location);
            }
        }

        listeners.clear();
    }

    protected void notifyLocationFail(@NonNull LocationErrorType errorType, @Nullable String reason) {
        cacheLocation = null;
        locationSuccessTime = 0;

        Log.d(TAG, "获取位置失败，开始回调("+ listeners.size() + "个Listeners)");

        for (LocationListener listener : listeners) {
            if (listener != null) {
                listener.onError(getLocationType(), errorType, reason);
            }
        }

        listeners.clear();
    }

    public void requestLocationContinuous() {
        isContinuous = true;

        startLocationContinuous();
    }

    public void requestLocation(LocationListener listener) {
        listeners.add(listener);

        if (cacheLocation != null && System.currentTimeMillis() - locationSuccessTime < 10 * 1000) {
            Log.d(TAG, "缓存有效，立即返回: " + cacheLocation);
            notifyLocationSuccess(cacheLocation);
            return;
        }

        Log.d(TAG, "没有缓存（或缓存无效），开启持续定位");
        requestLocationContinuous();
    }

    public void requestLocationOnce(LocationListener listener) {
        isContinuous = false;
        listeners.add(listener);

        if (cacheLocation != null && System.currentTimeMillis() - locationSuccessTime < 10 * 1000) {
            Log.d(TAG, "缓存有效，立即返回: " + cacheLocation);
            notifyLocationSuccess(cacheLocation);
            return;
        }

        Log.d(TAG, "没有缓存（或缓存无效）");
        startLocationOnce();
    }

    @NonNull
    protected abstract LocationType getLocationType();

    protected abstract void startLocationContinuous();

    // 是否持续定位依赖于位置回调处是否调用stopLocation方法，具体实现根据isContinuous字段判断
    protected abstract void startLocationOnce();

    public abstract void stopLocation();
}
