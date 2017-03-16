package com.example.android.location.loc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by heqingbao on 2017/3/15.
 */
public interface LocationListener {

    void onReceiveLocation(@NonNull LocationType locationType, @NonNull Location location);

    void onError(@NonNull LocationType locationType, @NonNull LocationErrorType errorType, @Nullable String reason);
}
