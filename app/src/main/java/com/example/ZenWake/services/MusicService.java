package com.example.ZenWake.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.zenwake.R;
import com.example.ZenWake.activities.MainActivity;
import com.example.ZenWake.ZenWakeApplication;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "MusicService";
    private static final int NOTIFICATION_ID = 2;

    private MediaPlayer mediaPlayer;
    private List<String> musicFiles;
    private int currentTrack = 0;
    private float volume = 0.5f;
    private ToneGenerator toneGenerator;
    private boolean usingBeep = false;
    private String selectedMusicPath = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MusicService onCreate");

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());
        mediaPlayer.setOnCompletionListener(this);

        // Start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, createNotification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MusicService onStartCommand");

        if (intent == null) return START_STICKY;

        String action = intent.getStringExtra("action");
        String folderPath = intent.getStringExtra("folder_path");
        String musicPath = intent.getStringExtra("music_path"); // Single selected music file
        int alarmId = intent.getIntExtra("alarm_id", -1);

        if (action != null) {
            handleMusicAction(action);
        } else if (musicPath != null && !musicPath.isEmpty()) {
            // Play single selected music file
            selectedMusicPath = musicPath;
            playSingleMusicFile(musicPath);
        } else if (folderPath != null && !folderPath.isEmpty()) {
            // Load music from folder (for backward compatibility)
            loadMusicFromFolder(folderPath);
            startPlayback();
        } else {
            // No music selected, play default sound
            Log.d(TAG, "No music selected, playing default sound");
            playDefaultSound();
        }

        return START_STICKY;
    }

    private void playSingleMusicFile(String filePath) {
        Log.d(TAG, "Playing selected music: " + filePath);
        try {
            // Stop any existing playback
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();

            // Set data source and play
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            mediaPlayer.setVolume(volume, volume);

            // Gradually increase volume
            new Thread(() -> {
                float vol = 0.1f;
                while (vol < volume && mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(vol, vol);
                    vol += 0.02f;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(volume, volume);
                }
            }).start();

        } catch (IOException e) {
            Log.e(TAG, "Error playing selected music: " + e.getMessage());
            playDefaultSound();
        }
    }

    private void loadMusicFromFolder(String folderPath) {
        musicFiles = new ArrayList<>();
        // In a real implementation, you'd scan the folder
        // For now, just add the path if it's a file
        if (folderPath.endsWith(".mp3") || folderPath.endsWith(".m4a") || folderPath.endsWith(".wav")) {
            musicFiles.add(folderPath);
        }
    }

    private void startPlayback() {
        if (musicFiles == null || musicFiles.isEmpty()) {
            playDefaultSound();
            return;
        }

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicFiles.get(currentTrack));
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setVolume(volume, volume);

            // Gradually increase volume
            new Thread(() -> {
                float vol = 0.1f;
                while (vol < volume && mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(vol, vol);
                    vol += 0.02f;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();

        } catch (IOException e) {
            Log.e(TAG, "Error playing music: " + e.getMessage());
            playDefaultSound();
        }
    }

    private void playDefaultSound() {
        Log.d(TAG, "Playing default notification sound");
        try {
            mediaPlayer.reset();
            Uri defaultSound = Settings.System.DEFAULT_NOTIFICATION_URI;
            mediaPlayer.setDataSource(this, defaultSound);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setVolume(volume, volume);
        } catch (Exception e) {
            Log.e(TAG, "Error playing default sound: " + e.getMessage());
            // Fallback to beep
            playBeep();
        }
    }

    private void playBeep() {
        try {
            usingBeep = true;
            toneGenerator = new ToneGenerator(android.media.AudioManager.STREAM_ALARM, (int)(volume * 50));

            new Thread(() -> {
                while (usingBeep && toneGenerator != null) {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 800);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Could not play beep: " + e.getMessage());
        }
    }

    private void handleMusicAction(String action) {
        switch (action) {
            case "SKIP":
                skipTrack();
                break;
            case "VOLUME_UP":
                volume = Math.min(1.0f, volume + 0.1f);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(volume, volume);
                }
                break;
            case "VOLUME_DOWN":
                volume = Math.max(0.1f, volume - 0.1f);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(volume, volume);
                }
                break;
            case "STOP":
                stopPlayback();
                stopSelf();
                break;
        }
    }

    private void skipTrack() {
        if (musicFiles != null && !musicFiles.isEmpty()) {
            currentTrack = (currentTrack + 1) % musicFiles.size();
            startPlayback();
        }
    }

    private void stopPlayback() {
        usingBeep = false;
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, ZenWakeApplication.CHANNEL_ID_MUSIC)
                .setContentTitle("Zen Wake")
                .setContentText("Alarm music playing")
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Loop the sound
        if (selectedMusicPath != null && !selectedMusicPath.isEmpty()) {
            playSingleMusicFile(selectedMusicPath);
        } else if (musicFiles != null && !musicFiles.isEmpty()) {
            currentTrack = (currentTrack + 1) % musicFiles.size();
            startPlayback();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MusicService onDestroy");
        stopPlayback();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}