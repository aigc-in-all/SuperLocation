package com.hqb.android.location.core.tencent;

import android.support.annotation.NonNull;

import com.hqb.android.location.Location;
import com.hqb.android.location.LocationErrorType;
import com.tencent.map.geolocation.TencentLocation;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class TencentLocationUtil {

    public static String getLocStr(String params, TencentLocation location, int error, String reason) {
        if (error == TencentLocation.ERROR_OK) {
//            mLocation = location;

            // 定位成功
            StringBuilder sb = new StringBuilder();
            sb.append("定位参数=").append(params).append("\n");
            sb.append("纬度=").append(location.getLatitude()).append("\n")
                    .append("经度=").append(location.getLongitude()).append("\n")
                    .append("精度=").append(location.getAccuracy()).append("\n")
                    .append("来源=").append(location.getProvider()).append("\n")
                    .append("省=").append(location.getProvince()).append("\n")
                    .append("城市=").append(location.getCity()).append("\n")
                    .append("行政区=").append(location.getDistrict()).append("\n")
                    .append("地址=").append(location.getAddress());

            return sb.toString();
        }

        return "错误";
    }

    public static String toString(int coordinateType) {
        if (coordinateType == com.tencent.map.geolocation.TencentLocationManager.COORDINATE_TYPE_GCJ02) {
            return "国测局坐标(火星坐标)";
        } else if (coordinateType == com.tencent.map.geolocation.TencentLocationManager.COORDINATE_TYPE_WGS84) {
            return "WGS84坐标(GPS坐标, 地球坐标)";
        } else {
            return "非法坐标";
        }
    }

    @NonNull
    public static Location parse(@NonNull TencentLocation location) {
        Location loc = new Location(location.getLongitude(), location.getLatitude());
        loc.setProvince(location.getProvince());
        loc.setCity(location.getCity());
        loc.setDistrict(location.getDistrict());
        loc.setStreet(location.getStreet());
        loc.setStreetNo(location.getStreetNo());

        // RequestLevel=1：包含经纬度, 位置名称, 位置地址
        // RequestLevel=3：包含经纬度，位置所处的中国大陆行政区划
        // 1的时候有address信息，但是没有province/city/district/street等信息
        // 3的时候没有address信息，但有province/city/district/street等信息
        // 所以这里使用3，address由我们手动拼装！！！
        // http://lbs.qq.com/geo/guide-use.html
        String address = location.getAddress();
        if (address == null || address.equals("")) {
            address = new StringBuilder()
                    .append(location.getProvince())
                    .append(location.getCity())
                    .append(location.getDistrict())
                    .append(location.getStreet())
                    .append(location.getStreetNo())
                    .toString();
        }

        loc.setAddress(address);

        // location.getTime()
        // 返回当前位置的生成时间.
        // http://lbs.qq.com/geosdk/doc/
        loc.setTime(location.getTime());
        return loc;
    }

    // http://lbs.qq.com/geo/guide-use.html
    @NonNull
    public static String initResultByCode(int code) {
        switch (code) {
            case 0:
                return "注册位置监听器成功";
            case 1:
                return "设备缺少使用腾讯定位SDK需要的基本条件";
            case 2:
                return "配置的 key 不正确";
            case 3:
                return "自动加载libtencentloc.so失败";
            default:
                return "未知错误";
        }
    }

    @NonNull
    public static LocationErrorType getErrorTypeByCode(int errorCode) {
        switch (errorCode) {
            case TencentLocation.ERROR_NETWORK:
                return LocationErrorType.NET_UNAVAILABLE;
            case TencentLocation.ERROR_BAD_JSON:
                return LocationErrorType.NO_PERMISSION;
            default:
                return LocationErrorType.UNKNOWN;
        }
    }

    // http://lbs.qq.com/geo/guide-use.html
    @NonNull
    public static String getReasonByErrorCode(int code) {
        switch (code) {
            case TencentLocation.ERROR_OK:
                return "定位成功";
            case TencentLocation.ERROR_NETWORK:
                return "网络问题引起的定位失败";
            case TencentLocation.ERROR_BAD_JSON:
                // GPS, Wi-Fi 或基站错误引起的定位失败：
                // 1、用户的手机确实采集不到定位凭据，比如偏远地区比如地下车库电梯内等;
                // 2、开关跟权限问题，比如用户关闭了位置信息，关闭了wifi，未授予app定位权限等。
                return "GPS, Wi-Fi 或基站错误引起的定位失败";
            case TencentLocation.ERROR_WGS84:
                return "无法将WGS84坐标转换成GCJ-02坐标时的定位失";
            case TencentLocation.ERROR_UNKNOWN:
                return "未知原因引起的定位失败";
            default:
                return "未知错误";
        }
    }
}
