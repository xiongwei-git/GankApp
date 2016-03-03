package com.android.ted.gank;

import android.app.Application;
import android.content.Context;
import com.orhanobut.logger.Logger;

public class GankApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initLogger();
    }

    private void initLogger() {
        Logger.init("xiongwei").hideThreadInfo().setLogValve(BuildConfig.LOG_DEBUG).methodCount(1);
    }
}
