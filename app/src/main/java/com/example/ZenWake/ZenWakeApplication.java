package com.example.ZenWake;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ZenWakeApplication extends Application {

    public static final String CHANNEL_ID_ALARM = "alarm_channel";
    public static final String CHANNEL_ID_MUSIC = "music_channel";
    public static final String CHANNEL_ID_SLEEP = "sleep_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Alarm channel
            NotificationChannel alarmChannel = new NotificationChannel(
                    CHANNEL_ID_ALARM,
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alarmChannel.setDescription("Channel for alarm notifications");
            alarmChannel.enableVibration(true);
            alarmChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            alarmChannel.setBypassDnd(true);
            // Removed setLockscreenVisibility - it will use default

            // Music channel
            NotificationChannel musicChannel = new NotificationChannel(
                    CHANNEL_ID_MUSIC,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            musicChannel.setDescription("Channel for music playback");
            musicChannel.setSound(null, null);

            // Sleep tracking channel
            NotificationChannel sleepChannel = new NotificationChannel(
                    CHANNEL_ID_SLEEP,
                    "Sleep Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            sleepChannel.setDescription("Channel for sleep tracking service");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(alarmChannel);
            manager.createNotificationChannel(musicChannel);
            manager.createNotificationChannel(sleepChannel);
        }
    }}