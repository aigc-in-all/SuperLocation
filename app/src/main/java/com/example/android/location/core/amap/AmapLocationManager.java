package com.example.android.location.core.amap;

import android.support.annotation.NonNull;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.android.location.MainApplication;
import com.example.android.location.core.AbsLocationManager;
import com.example.android.location.core.LocationErrorType;
import com.example.android.location.core.LocationListener;
import com.example.android.location.core.LocationType;

/**
 * Created by heqingbao on 2017/3/15.
 *
 * http://lbs.amap.com/api/android-location-sdk/guide/android-location/getlocation#start-location
 */
public class AmapLocationManager extends AbsLocationManager {

    private static final String TAG = "高德定位--->";

    private static AmapLocationManager instance = null;

    private AMapLocationClient locationClient = null;

    public static AmapLocationManager getInstance() {
        if (instance == null) {
            instance = new AmapLocationManager();
        }
        return instance;
    }

    private AmapLocationManager() {
    }

    @NonNull
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        option.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        option.setHttpTimeOut(10000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效。单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        option.setInterval(2000);//可选，设置定位间隔。默认为2秒
        option.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        option.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        option.setOnceLocationLatest(true);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        option.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        option.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        option.setWifiActiveScan(true); // 设置是否强制刷新WIFI，默认为强制刷新。每次定位主动刷新WIFI模块会提升WIFI定位精度，但相应的会多付出一些电量消耗。
        option.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
        option.setMockEnable(false); // 设置是否允许模拟软件Mock位置结果，多为模拟GPS定位结果，默认为false，不允许模拟位置。
        return option;
    }

    @Override
    public void requestLocationOnce(LocationListener listener) {
        // 每次定位后销毁client，需要再创建一个新的！
        locationClient = new AMapLocationClient(MainApplication.getContext());
        locationClient.setLocationOption(getDefaultOption());

        // start
        locationClient.setLocationListener(new InnerLocationListener(listener));
        locationClient.startLocation();
    }

    @Override
    public void stopLocation() {
        if (locationClient == null || !locationClient.isStarted()) {
            return;
        }

        //停止定位后，本地定位服务并不会被销毁
        locationClient.stopLocation();

        locationClient.stopAssistantLocation();

        // 销毁定位客户端，同时销毁本地定位服务。
        // 销毁定位客户端之后，若要重新开启定位请重新New一个AMapLocationClient对象。
        locationClient.onDestroy();
    }

    private class InnerLocationListener implements AMapLocationListener {

        private LocationListener listener;

        public InnerLocationListener(LocationListener listener) {
            this.listener = listener;
        }

        @Override
        public void onLocationChanged(AMapLocation loc) {
            Log.d(TAG, AmapLocationUtil.getLocationStr(loc));

            stopLocation();

            if (listener == null) {
                return;
            }

            if (loc == null) {
                String result = "定位失败，loc is null";
                Log.d(TAG, result);
                listener.onError(LocationType.AMAP, LocationErrorType.UNKNOWN, result);
                return;
            }

            if (loc.getErrorCode() != 0) {
                LocationErrorType errorType = AmapLocationUtil.getErrorTypeByCode(loc.getErrorCode());
                String errorReason = AmapLocationUtil.getReasonByCode(loc.getErrorCode());
                listener.onError(LocationType.AMAP, errorType, errorReason);
                return;
            }

            listener.onReceiveLocation(LocationType.AMAP, AmapLocationUtil.parse(loc));
        }
    }
}
