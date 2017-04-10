package com.hqb.android.location.core.amap;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.hqb.android.location.Location;
import com.hqb.android.location.LocationErrorType;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class AmapLocationUtil {

    public synchronized static String getLocationStr(AMapLocation location) {
        if (null == location) {
            return "定位失败，location == null";
        }

        StringBuilder sb = new StringBuilder();
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if (location.getErrorCode() == 0) {
            sb.append("定位成功" + "\n");
            sb.append("定位类型: " + location.getLocationType() + "\n");
            sb.append("经    度    : " + location.getLongitude() + "\n");
            sb.append("纬    度    : " + location.getLatitude() + "\n");
            sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
            sb.append("提供者    : " + location.getProvider() + "\n");

            sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
            sb.append("角    度    : " + location.getBearing() + "\n");
            // 获取当前提供定位服务的卫星个数
            sb.append("星    数    : " + location.getSatellites() + "\n");
            sb.append("国    家    : " + location.getCountry() + "\n");
            sb.append("省            : " + location.getProvince() + "\n");
            sb.append("市            : " + location.getCity() + "\n");
            sb.append("城市编码 : " + location.getCityCode() + "\n");
            sb.append("区            : " + location.getDistrict() + "\n");
            sb.append("区域 码   : " + location.getAdCode() + "\n");
            sb.append("地    址    : " + location.getAddress() + "\n");
            sb.append("兴趣点    : " + location.getPoiName() + "\n");
            //定位完成的时间
            sb.append("定位时间: " + formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
        } else {
            //定位失败
            sb.append("定位失败" + "\n");
            sb.append("错误码:" + location.getErrorCode() + "\n");
            sb.append("错误信息:" + location.getErrorInfo() + "\n");
            sb.append("错误描述:" + location.getLocationDetail() + "\n");
        }
        //定位之后的回调时间
        sb.append("回调时间: " + formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");
        return sb.toString();
    }

    private static SimpleDateFormat sdf = null;

    public synchronized static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }

    @NonNull
    public static Location parse(@NonNull AMapLocation location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        String address = location.getAddress();

        Location loc = new Location(longitude, latitude, address);
        loc.setTime(location.getTime());
        return loc;
    }

    @NonNull
    public static LocationErrorType getErrorTypeByCode(int errorCode) {
        switch (errorCode) {
            case 4:
            case 11:
                return LocationErrorType.NET_UNAVAILABLE;
            case 12:
            case 13:
                return LocationErrorType.NO_PERMISSION;
            default:
                return LocationErrorType.UNKNOWN;
        }
    }

    @NonNull
    public static String getReasonByCode(int errorCode) {
        switch (errorCode) {
            case 1:
                return "一些重要参数为空，如context；请对定位传递的参数进行非空判断";
            case 2:
                return "定位失败，由于仅扫描到单个wifi，且没有基站信息。请重新尝试";
            case 3:
                return "获取到的请求参数为空，可能获取过程中出现异常。请对所连接网络进行全面检查，请求可能被篡改";
            case 4:
                return "请求服务器过程中的异常，多为网络情况差，链路不通导致请检查设备网络是否通畅";
            case 5:
                return "返回的XML格式错误，解析失败。请稍后再试";
            case 6:
                return "定位服务返回定位失败。请将errorDetail（通过getLocationDetail()方法获取）信息通过工单系统反馈给我们";
            case 7:
                return "KEY鉴权失败。请仔细检查key绑定的sha1值与apk签名sha1值是否对应，或通过高频问题查找相关解决办法";
            case 8:
                return "Android exception常规错误请将errordetail（通过getLocationDetail()方法获取）信息通过工单系统反馈给我们";
            case 9:
                return "定位初始化时出现异常。请重新启动定位";
            case 10:
                return "定位客户端启动失败。请检查AndroidManifest.xml文件是否配置了APSService定位服务";
            case 11:
                return "定位时的基站信息错误。请检查是否安装SIM卡，设备很有可能连入了伪基站网络";
            case 12:
                return "缺少定位权限。请在设备的设置中开启app的定位权限";
            case 13:
                return "定位失败，由于设备未开启WIFI模块或未插入SIM卡，且GPS当前不可用。 建议开启设备的WIFI模块，并将设备中插入一张可以正常工作的SIM卡，或者检查GPS是否开启；如果以上都内容都确认无误，请您检查App是否被授予定位权限";
            case 14:
                return "S 定位失败，由于设备当前 GPS 状态差。 建议持设备到相对开阔的露天场所再次尝试";
            case 15:
                return "定位结果被模拟导致定位失败。 如果您希望位置被模拟，请通过setMockEnable(true);方法开启允许位置模拟";
            case 16:
                return "当前POI检索条件、行政区划检索条件下，无可用地理围栏。 建议调整检索条件后重新尝试，例如调整POI关键字，调整POI类型，调整周边搜区域，调整行政区关键字等";
            case 17:
                return "相同的地理围栏已经存在，无需重复添加";
            default:
                return "未知错误";
        }
    }
}
