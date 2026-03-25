package com.example.ZenWake.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.ZenWake.services.GuardianAngelService;

public class GuardianAngelReceiver extends BroadcastReceiver {

    private static final String TAG = "GuardianAngelReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Guardian Angel receiver triggered");

        int alarmId = intent.getIntExtra("alarm_id", -1);
        int delayMinutes = intent.getIntExtra("delay_minutes", 15);

        Intent serviceIntent = new Intent(context, GuardianAngelService.class);
        serviceIntent.putExtra("alarm_id", alarmId);
        serviceIntent.putExtra("delay_minutes", delayMinutes);

        context.startService(serviceIntent);
    }
}