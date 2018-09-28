package com.crearo.recordapplication;

import android.app.Application;
import android.content.res.Resources;

/**
 * Created by aa on 2018/4/27.
 */

public class MyApplication extends Application{

    public static final boolean DEBUG = true;
    private static Resources mRes;

    @Override
    public void onCreate() {
        super.onCreate();
        mRes = getResources();
    }

    public static Resources getAppResource() {
        return mRes;
    }
}
