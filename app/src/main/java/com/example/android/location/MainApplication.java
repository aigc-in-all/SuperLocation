package com.example.android.location;

import android.app.Application;
import android.content.Context;

/**
 * Created by heqingbao on 2017/3/15.
 */
public class MainApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
