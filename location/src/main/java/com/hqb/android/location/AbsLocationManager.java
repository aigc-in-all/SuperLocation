package com.hqb.android.location;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heqingbao on 2017/3/15.
 */
public abstract class AbsLocationManager {

    protected Context context;

    protected List<LocationListener> listeners = new ArrayList<>();

    public AbsLocationManager(Context context) {
        this.context = context;
    }

    protected void notifyLocationSuccess(@NonNull LocationType locationType, @NonNull Location location) {
        for (LocationListener listener : listeners) {
            if (listener != null) {
                listener.onReceiveLocation(locationType, location);
            }
        }

        listeners.clear();
    }

    protected void notifyLocationFail(@NonNull LocationType locationType, @NonNull LocationErrorType errorType, @Nullable String reason) {
        for (LocationListener listener : listeners) {
            if (listener != null) {
                listener.onError(locationType, errorType, reason);
            }
        }

        listeners.clear();
    }

    @CallSuper
    public void requestLocationOnce(LocationListener listener) {
        listeners.add(listener);
    }

    public abstract void stopLocation();
}
