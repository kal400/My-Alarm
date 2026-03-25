package com.example.ZenWake.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.example.zenwake.R;
import com.example.ZenWake.activities.MainActivity;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;
import com.example.ZenWake.utils.AlarmScheduler;

public class AlarmService extends Service {

    private static final String CHANNEL_ID = "alarm_service_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("RESCHEDULE_ALARMS".equals(action)) {
                rescheduleAlarms();
            }
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background service for alarms");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Zen Wake")
                .setContentText("Alarm service is running")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void rescheduleAlarms() {
        AppDatabase database = AppDatabase.getInstance(this);
        for (Alarm alarm : database.alarmDao().getEnabledAlarms()) {
            AlarmScheduler.scheduleAlarm(this, alarm);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}