package com.example.ZenWake.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.ZenWake.activities.AlarmRingingActivity;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;

public class SnoozeReceiver extends BroadcastReceiver {

    private static final String TAG = "SnoozeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Snooze alarm received");

        int alarmId = intent.getIntExtra("alarm_id", -1);

        if (alarmId == -1) {
            Log.e(TAG, "No alarm ID provided for snooze");
            return;
        }

        // Start alarm ringing activity for snooze
        Intent alarmIntent = new Intent(context, AlarmRingingActivity.class);
        alarmIntent.putExtra("alarm_id", alarmId);
        alarmIntent.putExtra("is_snooze", true);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(alarmIntent);
    }
}