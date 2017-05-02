package com.hqb.android.location;

import android.content.Context;
import android.support.annotation.NonNull;

import com.hqb.android.location.core.amap.AmapLocationManager;
import com.hqb.android.location.core.baidu.BaiduLocationManager;
import com.hqb.android.location.core.tencent.TencentLocationManager;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class LocationManager {

    private static LocationManager instance;

    private Context appContext;

    private LocationType locationType = LocationType.BAIDU;

    public static LocationManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new LocationManager(context);
        }
        return instance;
    }

    private LocationManager(@NonNull Context context) {
        appContext = context.getApplicationContext();
    }

    public void setLocationType(@NonNull LocationType locationType) {
        this.locationType = locationType;
    }

    @NonNull
    public LocationType getLocationType() {
        return locationType;
    }

    /**
     * 开启持续定位，无返回，持续获取到的位置会更新到缓存
     * 注意：
     * 1.一般与requestLocation(listener)方法配合使用
     * 2.在合适的时候调用stopLocation方法
     */
    public void requestLocationContinuous() {
        getLocationManager().requestLocationContinuous();
    }

    /**
     * 基于持续定位快速请求位置
     * 持续定位的结果会实时更新到缓存。
     * 1.如果缓存有效，直接使用缓存结果
     * 2.如果缓存无效，开启实时定位，返回第一次成功请求到的位置（并更新到缓存）
     */
    public void requestLocation(LocationListener listener) {
        getLocationManager().requestLocation(listener);
    }

    /**
     * 单次定位
     * 1.如果缓存有效，则返回缓存的位置
     * 2.如果缓存无效，开启单次定位（首次定位成功后会stop，非持续定位）
     */
    public void requestLocationOnce(LocationListener listener) {
        getLocationManager().requestLocationOnce(listener);
    }

    private AbsLocationManager getLocationManager() {
        switch (locationType) {
            case AMAP:
                return AmapLocationManager.getInstance(appContext);
            case BAIDU:
                return BaiduLocationManager.getInstance(appContext);
            case TENCENT:
                return TencentLocationManager.getInstance(appContext);
            default:
                throw new RuntimeException("Unsupported location type : " + locationType);
        }
    }

    public void stopLocation() {
        getLocationManager().stopLocation();
    }


}
