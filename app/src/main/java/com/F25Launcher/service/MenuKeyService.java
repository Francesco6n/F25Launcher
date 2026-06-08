package com.F25Launcher.service;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.F25Launcher.utils.Constants;

public class MenuKeyService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU
                && event.getAction() == KeyEvent.ACTION_DOWN
                && isForeground()) {
            Intent intent = new Intent(Constants.MENU_KEY_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            return true;
        }
        return super.onKeyEvent(event);
    }

    private boolean isForeground() {
        ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(info);
        return info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                || info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
    }
}
