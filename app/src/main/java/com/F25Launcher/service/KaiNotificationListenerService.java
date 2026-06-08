package com.F25Launcher.service;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.F25Launcher.utils.Constants;

import java.util.HashSet;

public class KaiNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "KaiNotificationListener";
    private final HashSet<String> activeNotifications = new HashSet<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        Log.d(TAG, "Notification posted from: " + pkg + " - " + sbn.getNotification().tickerText);
        activeNotifications.add(sbn.getKey());
        broadcastCount();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        activeNotifications.remove(sbn.getKey());
        broadcastCount();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        activeNotifications.clear();
        StatusBarNotification[] active = getActiveNotifications();
        if (active != null) {
            for (StatusBarNotification sbn : active) {
                activeNotifications.add(sbn.getKey());
            }
        }
        broadcastCount();
    }

    private void broadcastCount() {
        Intent intent = new Intent(Constants.NOTIFICATION_COUNT_ACTION);
        intent.putExtra(Constants.NOTIFICATION_EXTRA_COUNT, activeNotifications.size());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
