package com.example.android.location.core.tencent;

import android.util.Log;

import com.example.android.location.MainApplication;
import com.example.android.location.core.AbsLocationManager;
import com.example.android.location.core.LocationErrorType;
import com.example.android.location.core.LocationListener;
import com.example.android.location.core.LocationType;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationRequest;

/**
 * Created by heqingbao on 2017/3/15.
 *
 * http://lbs.qq.com/geo/guide-install.html
 */
public class TencentLocationManager extends AbsLocationManager {
    private static final String TAG = "腾讯定位--->";

    private static TencentLocationManager instance = null;

    private InnerLocationListener innerLocationListener;

    private com.tencent.map.geolocation.TencentLocationManager locationManager;
    private String locParams; // 用于Log

    private boolean cellStatusDenied;// 蜂窝模块被关闭
    private boolean wifiStatusDenied;// wifi模块被关闭
    private boolean gpsStatusDenied;// gps模块被关闭

    public static TencentLocationManager getInstance() {
        if (instance == null) {
            instance = new TencentLocationManager();
        }
        return instance;
    }

    private TencentLocationManager() {
        locationManager = com.tencent.map.geolocation.TencentLocationManager.getInstance(MainApplication.getContext());
        // 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
        locationManager.setCoordinateType(com.tencent.map.geolocation.TencentLocationManager.COORDINATE_TYPE_GCJ02);
    }

    @Override
    public void requestLocationOnce(LocationListener listener) {

        resetStatus();

        TencentLocationRequest request = TencentLocationRequest.create();

        // 包含经纬度, 位置名称, 位置地址
        request.setRequestLevel(1);

        // 设置定位周期(位置监听器回调周期), 单位为 ms (毫秒)
        // 未提供单次定位api，这里在获取到位置到主动调用stop
        request.setInterval(10000);

        // 禁用缓存
        request.setAllowCache(false);

        // 允许使用GPS定位
        request.setAllowGPS(true);

        locParams = request.toString() + ", 坐标系=" + TencentLocationUtil.toString(locationManager.getCoordinateType());

        innerLocationListener = new InnerLocationListener(listener);
        int result = locationManager.requestLocationUpdates(request, innerLocationListener);

        if (result != 0) {
            // 注册失败
            if (listener != null) {
                listener.onError(LocationType.TENCENT, LocationErrorType.UNKNOWN, TencentLocationUtil.initResultByCode(result));
            }
        }
    }

    @Override
    public void stopLocation() {
        if (locationManager == null) {
            return;
        }

        if (innerLocationListener != null) {
            locationManager.removeUpdates(innerLocationListener);
            innerLocationListener = null;
        }
    }

    // 重置相关模块状态，假设各模块都没被关闭
    private void resetStatus() {
        cellStatusDenied = false;
        wifiStatusDenied = false;
        gpsStatusDenied = false;
    }

    // 测试结果：先回调三次：onStatusUpdate，再回调onLocationChanged
    private class InnerLocationListener implements TencentLocationListener {

        private LocationListener listener;

        public InnerLocationListener(LocationListener listener) {
            this.listener = listener;
        }

        @Override
        public void onLocationChanged(TencentLocation location, int error, String reason) {
            String result = TencentLocationUtil.getLocStr(locParams, location, error, reason);
            Log.d(TAG, result);

            stopLocation();

            if (listener == null) {
                return;
            }

            if (error == TencentLocation.ERROR_OK) { // 定位成功
                listener.onReceiveLocation(LocationType.TENCENT, TencentLocationUtil.parse(location));
                return;
            }

            LocationErrorType errorType = TencentLocationUtil.getErrorTypeByCode(error);
            String errorReason = TencentLocationUtil.getReasonByErrorCode(error);
            listener.onError(LocationType.TENCENT, errorType, errorReason);
        }

        /**
         * @param name   GPS，Wi-Fi等
         * @param status 新的状态, 启用或禁用
         * @param desc   状态描述
         */
        @Override
        public void onStatusUpdate(String name, int status, String desc) {
            Log.d(TAG, name + ", " + status + ", " + desc);

            if ("cell".equals(name) && (status == 0 || status == 2)) {
                cellStatusDenied = true;
            } else if ("wifi".equals(name) && (status == 0 || status == 2)) {
                wifiStatusDenied = true;
            } else if ("gps".equals(name) && (status == 0 || status == 4)) {
                gpsStatusDenied = true;
            }

            if (cellStatusDenied && wifiStatusDenied && gpsStatusDenied) {
                /*
                stopLocation();
                这里直接调用stopLocation()去停止定位会抛异常：
                E/AndroidRuntime: FATAL EXCEPTION: main
                      Process: com.example.android.location, PID: 26007
                      java.util.ConcurrentModificationException
                          at java.util.ArrayList$ArrayListIterator.next(ArrayList.java:573)
                          at ct.bk.c(TL:348)
                          at ct.ca.d(TL:262)
                          at ct.ca.onProviderDisabled(TL:383)
                          at android.location.LocationManager$ListenerTransport._handleMessage(LocationManager.java:299)

                暂不清楚异常原因。
                这里简单对listener置null，待onLocationChanged方法回调的时候做处理！！！
                */

                if (listener != null) {
                    listener.onError(LocationType.TENCENT, LocationErrorType.NO_PERMISSION, "蜂窝、WIFI、GPS模块都没打开");
                    listener = null;
                }
            }
        }
    }
}
