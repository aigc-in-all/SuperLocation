package com.example.android.location;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.example.android.location.core.Location;
import com.example.android.location.core.LocationErrorType;
import com.example.android.location.core.LocationListener;
import com.example.android.location.core.LocationManager;
import com.example.android.location.core.LocationType;
import com.example.android.location.core.amap.AmapLocationManager;
import com.example.android.location.core.baidu.BaiduLocationManager;
import com.example.android.location.core.tencent.TencentLocationManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private TextView tvResultView;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResultView = (TextView) findViewById(R.id.tv_result);
    }

    // 使用高德定位
    public void clickOnAmapLocation(View view) {
        resetViewState(LocationType.AMAP);
        AmapLocationManager.getInstance().requestLocationOnce(listener);
    }

    // 使用百度定位
    public void clickOnBaiduLocation(View view) {
        resetViewState(LocationType.BAIDU);
        BaiduLocationManager.getInstance().requestLocationOnce(listener);
    }

    // 使用腾讯定位
    public void clickOnTencentLocation(View view) {
        resetViewState(LocationType.TENCENT);
        TencentLocationManager.getInstance().requestLocationOnce(listener);
    }

    // All In One
    public void clickOnAllLocation(View view) {
        resetViewState(null);
        LocationManager.getInstance().requestLocationAllInOne(listener);
    }

    private void resetViewState(LocationType locationType) {
        if (locationType == null) {
            tvResultView.setText("All In One ...");
        } else {
            tvResultView.setText(String.format("正在使用[%s]定位...", locationType.getDesc()));
        }
    }

    private void displayLocation(LocationType locationType, Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append(locationType.getDesc());
        sb.append("\n").append("经度：").append(location.getLongitude());
        sb.append("\n").append("纬度：").append(location.getLatitude());
        sb.append("\n").append("地址：").append(location.getAddress());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss sss");
        sb.append("\n").append("时间：").append(sdf.format(new Date(location.getTime())));

        tvResultView.setText(sb.toString());
    }

    LocationListener listener = new LocationListener() {

        @Override
        public void onReceiveLocation(@NonNull final LocationType locationType, @NonNull final Location location) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    displayLocation(locationType, location);
                }
            });
        }

        @Override
        public void onError(@NonNull LocationType locationType, @NonNull LocationErrorType errorType, @Nullable String reason) {
            final StringBuilder sb = new StringBuilder(locationType.getDesc());
            sb.append("\n");
            sb.append(errorType.name());
            sb.append("\n");
            if (reason != null) {
                sb.append(reason);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tvResultView.setText(sb.toString());
                }
            });
        }
    };
}
