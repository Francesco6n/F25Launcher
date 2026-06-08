package com.F25Launcher;

import android.app.Application;

import com.F25Launcher.utils.BuglyUtils;

/**
 * @author Maribel
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BuglyUtils.initBugly(this);
    }
}
