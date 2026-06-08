package com.F25Launcher.utils;

import android.content.Context;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;

import com.F25Launcher.BuildConfig;

public class BuglyUtils {
    private static final String TAG = BuglyUtils.class.getSimpleName();
    public static void initBugly(Context context) {
        Log.i(TAG, "initBugly: ");
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppReportDelay(10000);
        CrashReport.initCrashReport(context.getApplicationContext(), strategy);
        CrashReport.setIsDevelopmentDevice(context, BuildConfig.DEBUG);
    }

}
