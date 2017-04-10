package com.hqb.android.location.core.baidu;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.hqb.android.location.AbsLocationManager;
import com.hqb.android.location.LocationErrorType;
import com.hqb.android.location.LocationListener;
import com.hqb.android.location.LocationType;

/**
 * Created by heqingbao on 2017/3/15.
 *
 * http://lbsyun.baidu.com/index.php?title=android-locsdk/guide/getloc
 */
public class BaiduLocationManager extends AbsLocationManager {
    private static final String TAG = "百度定位--->";

    private static BaiduLocationManager instance = null;

    private LocationClient locationClient;
    private InnerLocationListener innerLocationListener;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public static BaiduLocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new BaiduLocationManager(context);
        }
        return instance;
    }

    private BaiduLocationManager(Context context) {
        super(context);
    }

    @NonNull
    private LocationClientOption getDefaultOption() {
        LocationClientOption option = new LocationClientOption();

        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选，默认gcj02，设置返回的定位结果坐标系，官方Demo中用的是bd09ll
        option.setCoorType("gcj02"); // 国测局02年发布的坐标体系，又称『火星坐标』

        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(0);

        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);

        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);

        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(true);

        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(false);

        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(false);

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);

        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);

        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setEnableSimulateGps(false);

        return option;
    }

    @Override
    public void requestLocationOnce(LocationListener listener) {
        super.requestLocationOnce(listener);

        locationClient = new LocationClient(context);

        locationClient.setLocOption(getDefaultOption());

        innerLocationListener = new InnerLocationListener();
        locationClient.registerLocationListener(innerLocationListener);

        locationClient.start();
    }

    @Override
    public void stopLocation() {
        if (locationClient == null || !locationClient.isStarted()) {
            return;
        }

        if (innerLocationListener != null) {
            locationClient.unRegisterLocationListener(innerLocationListener);
            innerLocationListener = null;
        }

        locationClient.stop();
        locationClient = null;
    }

    // 注意回调在子线程!!!
    // Bug：连续两次（或多次）调用requestLocationUpdates，重复几次，onReceiveLocation的回调次数太多，无规律。。。
    private class InnerLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            Log.d(TAG, BaiduLocationUtil.getLocStr(bdLocation));

            stopLocation();

            if (bdLocation == null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyLocationFail(LocationType.BAIDU, LocationErrorType.UNKNOWN, "定位失败，location == null");
                    }
                });
                return;
            }

            // 61: GPS定位结果，GPS定位成功
            // 161: 网络定位结果，网络定位成功
            if (bdLocation.getLocType() == 61 || bdLocation.getLocType() == 161) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyLocationSuccess(LocationType.BAIDU, BaiduLocationUtil.parse(bdLocation));
                    }
                });
                return;
            }

            final LocationErrorType errorType = BaiduLocationUtil.getErrorTypeByCode(bdLocation.getLocType());
            final String errorReason = BaiduLocationUtil.getReasonByCode(bdLocation.getLocType());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyLocationFail(LocationType.BAIDU, errorType, errorReason);
                }
            });
        }

        // 回调连接wifi是否是移动热点
        // http://wiki.lbsyun.baidu.com/cms/androidloc/doc/v7.1/index.html
        @Override
        public void onConnectHotSpotMessage(String connectWifiMac, int hotSpotState) {
            Log.d(TAG, "onConnectHotSpotMessage: " + connectWifiMac + ", " + hotSpotState);
        }
    }
}
