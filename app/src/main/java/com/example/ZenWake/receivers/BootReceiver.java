package com.example.ZenWake.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;
import com.example.ZenWake.utils.AlarmScheduler;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed - rescheduling alarms");

            // Reschedule all enabled alarms
            AppDatabase database = AppDatabase.getInstance(context);
            List<Alarm> alarms = database.alarmDao().getEnabledAlarms();

            for (Alarm alarm : alarms) {
                AlarmScheduler.scheduleAlarm(context, alarm);
                Log.d(TAG, "Rescheduled alarm: " + alarm.getId());
            }
        }
    }
}