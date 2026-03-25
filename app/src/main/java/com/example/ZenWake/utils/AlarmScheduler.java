package com.example.ZenWake.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.ZenWake.models.Alarm;
import com.example.ZenWake.receivers.AlarmReceiver;
import java.util.Calendar;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    public static void scheduleAlarm(Context context, Alarm alarm) {
        if (!alarm.isEnabled()) {
            cancelAlarm(context, alarm);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.setAction("ALARM_" + alarm.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();

        if (triggerTime <= currentTime) {
            triggerTime += AlarmManager.INTERVAL_DAY;
        }

        Log.d(TAG, "Scheduling alarm " + alarm.getId() + " for " + new java.util.Date(triggerTime));

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
            Log.d(TAG, "Alarm scheduled successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm: " + e.getMessage());
            // Fallback to inexact alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    public static void cancelAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.d(TAG, "Cancelled alarm " + alarm.getId());
    }

    public static void scheduleSnooze(Context context, int alarmId, long snoozeTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarmId);
        intent.putExtra("is_snooze", true);
        intent.setAction("SNOOZE_" + alarmId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
            }
            Log.d(TAG, "Snooze scheduled for alarm " + alarmId);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling snooze: " + e.getMessage());
        }
    }

    // ADD THIS METHOD - for scheduling next occurrence of repeating alarms
    public static void scheduleNextOccurrence(Context context, Alarm alarm) {
        if (!alarm.isRepeating()) {
            Log.d(TAG, "Alarm is not repeating, no next occurrence to schedule");
            return;
        }

        Log.d(TAG, "Scheduling next occurrence for repeating alarm: " + alarm.getId());

        // Calculate next occurrence based on repeating days
        Calendar now = Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        alarmTime.set(Calendar.MINUTE, alarm.getMinute());
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        boolean[] days = alarm.getRepeatingDays();
        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday

        // Find next day
        for (int i = 1; i <= 7; i++) {
            int checkDay = (currentDayOfWeek + i) % 7;
            if (days[checkDay]) {
                // Found next day
                Calendar nextAlarmTime = (Calendar) alarmTime.clone();
                nextAlarmTime.add(Calendar.DAY_OF_YEAR, i);

                Log.d(TAG, "Next occurrence on day " + checkDay + " at " + nextAlarmTime.getTime().toString());

                // Schedule the alarm for that day
                scheduleAlarm(context, alarm);
                return;
            }
        }

        Log.d(TAG, "No next occurrence found for alarm");
    }
}