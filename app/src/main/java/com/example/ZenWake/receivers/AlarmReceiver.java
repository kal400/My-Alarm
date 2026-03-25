package com.example.ZenWake.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.zenwake.R;
import com.example.ZenWake.activities.AlarmRingingActivity;
import com.example.ZenWake.ZenWakeApplication;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received! Action: " + intent.getAction());

        int alarmId = intent.getIntExtra("alarm_id", -1);
        boolean isSnooze = intent.getBooleanExtra("is_snooze", false);

        Log.d(TAG, "Alarm ID: " + alarmId + ", isSnooze: " + isSnooze);

        // Wake up the device
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE,
                "ZenWake:AlarmLock");
        wakeLock.acquire(10000);

        try {
            // Create intent for the alarm activity
            Intent alarmIntent = new Intent(context, AlarmRingingActivity.class);
            alarmIntent.putExtra("alarm_id", alarmId);
            alarmIntent.putExtra("is_snooze", isSnooze);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // For Android 10+, use full-screen intent to bypass background restrictions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                showFullScreenNotification(context, alarmIntent, alarmId);
            } else {
                // For older versions, start activity directly
                context.startActivity(alarmIntent);
            }

            Log.d(TAG, "Alarm activity triggered");

        } catch (Exception e) {
            Log.e(TAG, "Error starting alarm activity: " + e.getMessage());
            e.printStackTrace();
        } finally {
            wakeLock.release();
        }
    }

    private void showFullScreenNotification(Context context, Intent intent, int alarmId) {
        // Create a pending intent for the alarm activity
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification with full-screen intent using your existing channel
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ZenWakeApplication.CHANNEL_ID_ALARM)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarm")
                .setContentText("Your alarm is ringing")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID + alarmId, builder.build());
            Log.d(TAG, "Full-screen notification shown for alarm " + alarmId);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error: " + e.getMessage());
            // Fallback - try to start activity directly
            context.startActivity(intent);
        }
    }
}