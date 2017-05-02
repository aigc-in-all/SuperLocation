package com.hqb.android.location.core.tencent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hqb.android.location.AbsLocationManager;
import com.hqb.android.location.Location;
import com.hqb.android.location.LocationErrorType;
import com.hqb.android.location.LocationType;
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

    private boolean isRequesting;

    public static TencentLocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new TencentLocationManager(context);
        }
        return instance;
    }

    private TencentLocationManager(Context context) {
        super(context);
        locationManager = com.tencent.map.geolocation.TencentLocationManager.getInstance(context);
        // 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
        locationManager.setCoordinateType(com.tencent.map.geolocation.TencentLocationManager.COORDINATE_TYPE_GCJ02);
    }

    @NonNull
    @Override
    protected LocationType getLocationType() {
        return LocationType.TENCENT;
    }

    @Override
    public void startLocationContinuous() {

        Log.d(TAG, ">>> 开始持续请求位置（如果是单次定位，位置请求成功后会关闭请求） <<<");

        if (isRequesting) {
            Log.d(TAG, "正在请求位置，略过本次请求！！！");
            return;
        }

        isRequesting = true;

        resetStatus();

        TencentLocationRequest request = TencentLocationRequest.create();

        // 包含经纬度，位置所处的中国大陆行政区划
        request.setRequestLevel(3);

        // 设置定位周期(位置监听器回调周期), 单位为 ms (毫秒)
        // 未提供单次定位api，这里在获取到位置到主动调用stop
        request.setInterval(SCAN_INTERVAL);

        // 禁用缓存
        request.setAllowCache(false);

        // 是否允许使用GPS定位
        request.setAllowGPS(true);

        locParams = request.toString() + ", 坐标系=" + TencentLocationUtil.toString(locationManager.getCoordinateType());

        innerLocationListener = new InnerLocationListener();
        int result = locationManager.requestLocationUpdates(request, innerLocationListener);

        if (result != 0) {
            // 注册失败
            notifyLocationFail(LocationErrorType.UNKNOWN, TencentLocationUtil.initResultByCode(result));
        }
    }

    @Override
    public void startLocationOnce() {
        Log.d(TAG, ">>> 开始请求位置 <<<");

        startLocationContinuous();
    }

    @Override
    public void stopLocation() {
        Log.d(TAG, ">>> 停止请求位置 <<<");

        isRequesting = false;

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
    // Bug：连续两次调用requestLocationUpdates，重复两次，第三次不会调用onLocationChanged方法，而是下一次调用requestLocationUpdates的时候一起回调。。。
    private class InnerLocationListener implements TencentLocationListener {

        private volatile boolean isPermissionDeny = false;

        public InnerLocationListener() {
            isPermissionDeny = false;
        }

        @Override
        public void onLocationChanged(TencentLocation tLocation, int error, String reason) {
            Log.d(TAG, TencentLocationUtil.getLocStr(locParams, tLocation, error, reason));

            if (!isContinuous) {
                stopLocation();
            }

            if (isPermissionDeny) {
                stopLocation();
                notifyLocationFail(LocationErrorType.NO_PERMISSION, "蜂窝、WIFI、GPS模块都没打开");
                return;
            }

            if (error == TencentLocation.ERROR_OK) { // 定位成功
                Location location = TencentLocationUtil.parse(tLocation);
                notifyLocationSuccess(location);
                return;
            }

            stopLocation();

            LocationErrorType errorType = TencentLocationUtil.getErrorTypeByCode(error);
            String errorReason = TencentLocationUtil.getReasonByErrorCode(error);
            notifyLocationFail(errorType, errorReason);
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
                */

                isPermissionDeny = true;
            }
        }
    }
}
