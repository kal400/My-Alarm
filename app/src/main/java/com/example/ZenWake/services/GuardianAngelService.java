package com.example.ZenWake.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.zenwake.R;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;
import java.util.Timer;
import java.util.TimerTask;

public class GuardianAngelService extends Service {

    private static final String TAG = "GuardianAngelService";
    private MediaPlayer mediaPlayer;
    private PowerManager.WakeLock wakeLock;
    private Timer volumeTimer;
    private int alarmId;
    private int delayMinutes;
    private float currentVolume = 0.3f;
    private String selectedMusicPath;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Guardian Angel Service created");

        // Acquire wake lock to keep CPU running
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ZenWake:GuardianAngelWakeLock"
        );
        wakeLock.acquire(10 * 60 * 1000L);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            alarmId = intent.getIntExtra("alarm_id", -1);
            delayMinutes = intent.getIntExtra("delay_minutes", 15);
            selectedMusicPath = intent.getStringExtra("music_path");

            Log.d(TAG, "Guardian Angel started for alarm: " + alarmId + " with delay: " + delayMinutes);

            // Start monitoring
            startMonitoring();
        }
        return START_STICKY;
    }

    private void startMonitoring() {
        // Schedule volume increase over time
        volumeTimer = new Timer();
        volumeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                increaseVolume();
            }
        }, delayMinutes * 60 * 1000, 30 * 1000); // Start after delay, then every 30 seconds
    }

    private void increaseVolume() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentVolume = Math.min(1.0f, currentVolume + 0.1f);
            mediaPlayer.setVolume(currentVolume, currentVolume);
            Log.d(TAG, "Guardian Angel increasing volume to: " + currentVolume);

            if (currentVolume >= 1.0f) {
                // Max volume reached, play something even louder
                playEmergencySound();
            }
        } else {
            // Start playing if not playing
            playMusic();
        }
    }

    private void playMusic() {
        try {
            // Try to play the selected music if available
            if (selectedMusicPath != null && !selectedMusicPath.isEmpty()) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(selectedMusicPath);
                mediaPlayer.prepare();
            } else {
                // Fallback to default alarm sound
                mediaPlayer = MediaPlayer.create(this, R.raw.pleasant_alarm);
            }

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(currentVolume, currentVolume);
                mediaPlayer.start();
                Log.d(TAG, "Guardian Angel started playing music");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing music: " + e.getMessage());
        }
    }

    private void playEmergencySound() {
        // This is the final wake-up attempt - use the loudest sound
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
            Log.d(TAG, "Guardian Angel - EMERGENCY MAX VOLUME!");
        } catch (Exception e) {
            Log.e(TAG, "Error playing emergency sound: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Guardian Angel Service destroyed");

        if (volumeTimer != null) {
            volumeTimer.cancel();
            volumeTimer = null;
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}