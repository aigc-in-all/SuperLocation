package com.example.android.location.core.baidu;

import android.support.annotation.NonNull;

import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.example.android.location.core.Location;
import com.example.android.location.core.LocationErrorType;

import java.util.List;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class BaiduLocationUtil {

    public static String getLocStr(BDLocation location) {
        //获取定位结果
        StringBuffer sb = new StringBuffer(256);

        sb.append("time : ");
        sb.append(location.getTime());    //获取定位时间

        sb.append("\nerror code : ");
        sb.append(location.getLocType());    //获取类型类型

        sb.append("\nlatitude : ");
        sb.append(location.getLatitude());    //获取纬度信息

        sb.append("\nlontitude : ");
        sb.append(location.getLongitude());    //获取经度信息

        sb.append("\nradius : ");
        sb.append(location.getRadius());    //获取定位精准度

        if (location.getLocType() == BDLocation.TypeGpsLocation) {

            // GPS定位结果
            sb.append("\nspeed : ");
            sb.append(location.getSpeed());    // 单位：公里每小时

            sb.append("\nsatellite : ");
            sb.append(location.getSatelliteNumber());    //获取卫星数

            sb.append("\nheight : ");
            sb.append(location.getAltitude());    //获取海拔高度信息，单位米

            sb.append("\ndirection : ");
            sb.append(location.getDirection());    //获取方向信息，单位度

            sb.append("\naddr : ");
            sb.append(location.getAddrStr());    //获取地址信息

            sb.append("\ndescribe : ");
            sb.append("gps定位成功");

        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

            // 网络定位结果
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());    //获取地址信息

            sb.append("\noperationers : ");
            sb.append(location.getOperators());    //获取运营商信息

            sb.append("\ndescribe : ");
            sb.append("网络定位成功");

        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

            // 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");

        } else if (location.getLocType() == BDLocation.TypeServerError) {

            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");

        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

        }

        sb.append("\nlocationdescribe : ");
        sb.append(location.getLocationDescribe());    //位置语义化信息

        List<Poi> list = location.getPoiList();    // POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }
        }

        return sb.toString();
    }

    @NonNull
    public static Location parse(@NonNull BDLocation location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        String address = location.getAddrStr();

        Location loc = new Location(longitude, latitude, address);
        try {
            // server返回的当前定位时间
            // http://wiki.lbsyun.baidu.com/cms/androidloc/doc/v7.1/index.html
            long time = Long.valueOf(location.getTime());
            loc.setTime(time);
        } catch (Exception ignore) {
        }
        return loc;
    }

    @NonNull
    public static LocationErrorType getErrorTypeByCode(int errorCode) {
        switch (errorCode) {
            case 63:
                return LocationErrorType.NET_UNAVAILABLE;
            case 62:
            case 167:
                return LocationErrorType.NO_PERMISSION;
            default:
                return LocationErrorType.UNKNOWN;
        }
    }

    // http://lbsyun.baidu.com/index.php?title=android-locsdk/guide/getloc
    @NonNull
    public static String getReasonByCode(int errorCode) {
        switch (errorCode) {
            case 61:
                return "GPS定位结果，GPS定位成功。";
            case 62:
                return "无法获取有效定位依据，定位失败，请检查运营商网络或者WiFi网络是否正常开启，尝试重新请求定位。";
            case 63:
                return "网络异常，没有成功向服务器发起请求，请确认当前测试手机网络是否通畅，尝试重新请求定位。";
            case 65:
                return "定位缓存的结果。";
            case 66:
                return "离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果。";
            case 67:
                return "离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果。";
            case 68:
                return "网络连接失败时，查找本地离线定位时对应的返回结果。";
            case 161:
                return "网络定位结果，网络定位成功。";
            case 162:
                return "请求串密文解析失败，一般是由于客户端SO文件加载失败造成，请严格参照开发指南或demo开发，放入对应SO文件。";
            case 167:
                return "服务端定位失败，请您检查是否禁用获取位置信息权限，尝试重新请求定位。";
            case 502:
                return "AK参数错误，请按照说明文档重新申请AK。";
            case 505:
                return "AK不存在或者非法，请按照说明文档重新申请AK。";
            case 601:
                return "AK服务被开发者自己禁用，请按照说明文档重新申请AK。";
            case 602:
                return "key mcode不匹配，您的AK配置过程中安全码设置有问题，请确保：SHA1正确，“;”分号是英文状态；且包名是您当前运行应用的包名，请按照说明文档重新申请AK。";
            default:
                // 501～700：AK验证失败，请按照说明文档重新申请AK。
                return "未知错误";
        }
    }
}
