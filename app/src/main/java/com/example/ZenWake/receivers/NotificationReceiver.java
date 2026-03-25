package com.example.ZenWake.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification action received");

        String action = intent.getAction();
        int notificationId = intent.getIntExtra("notification_id", -1);

        if ("DISMISS".equals(action)) {
            // Dismiss notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        } else if ("SNOOZE".equals(action)) {
            // Handle snooze action
            Log.d(TAG, "Snooze action triggered");
        }
    }
}