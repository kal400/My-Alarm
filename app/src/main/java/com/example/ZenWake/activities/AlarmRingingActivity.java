package com.example.ZenWake.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.zenwake.R;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;
import com.example.ZenWake.services.MusicService;
import com.example.ZenWake.utils.AlarmScheduler;
import com.example.ZenWake.utils.ChallengeManager;
import com.example.ZenWake.utils.WeatherManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AlarmRingingActivity extends AppCompatActivity {

    private TextView alarmTime, weatherInfo, nextEvent, challengeQuestion;
    private CardView challengeCard;
    private TextView btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, btnClear, btnSubmit;
    private LinearLayout snoozeButton, musicButton;
    private View sunriseOverlay;

    private int alarmId;
    private Alarm currentAlarm;
    private AppDatabase database;
    private ChallengeManager challengeManager;
    private WeatherManager weatherManager;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;

    private String currentAnswer = "";
    private int correctAnswer;
    private Handler handler = new Handler();
    private Runnable sunriseRunnable;
    private float sunriseProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure activity shows on lock screen and turns screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.activity_alarm_ringing);

        // Get alarm ID from intent
        alarmId = getIntent().getIntExtra("alarm_id", -1);
        database = AppDatabase.getInstance(this);

        initViews();
        setupWakeLock();
        setupVibrator();

        // Load alarm in background
        new LoadAlarmTask().execute(alarmId);
    }

    private class LoadAlarmTask extends AsyncTask<Integer, Void, Alarm> {
        @Override
        protected Alarm doInBackground(Integer... params) {
            return database.alarmDao().getAlarmById(params[0]);
        }

        @Override
        protected void onPostExecute(Alarm alarm) {
            currentAlarm = alarm;
            if (currentAlarm != null) {
                setupAlarm();
            }
        }
    }

    private void setupAlarm() {
        challengeManager = new ChallengeManager(currentAlarm.getDifficulty());
        weatherManager = new WeatherManager(this);

        updateTime();
        updateWeather();
        generateChallenge();
        startSunriseAnimation();
        setupMediaPlayer();
    }

    private void initViews() {
        alarmTime = findViewById(R.id.alarmTime);
        weatherInfo = findViewById(R.id.weatherInfo);
        nextEvent = findViewById(R.id.nextEvent);
        challengeCard = findViewById(R.id.challengeCard);
        challengeQuestion = findViewById(R.id.challengeQuestion);
        sunriseOverlay = findViewById(R.id.sunriseOverlay);

        // Keypad buttons
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btn0 = findViewById(R.id.btn0);
        btnClear = findViewById(R.id.btnClear);
        btnSubmit = findViewById(R.id.btnSubmit);

        snoozeButton = findViewById(R.id.snoozeButton);
        musicButton = findViewById(R.id.musicButton);

        setupClickListeners();
    }

    private void setupClickListeners() {
        View.OnClickListener numberListener = v -> {
            TextView tv = (TextView) v;
            currentAnswer += tv.getText().toString();
            challengeQuestion.setText(currentAnswer);
            if (vibrator != null) {
                vibrator.vibrate(50);
            }
        };

        btn1.setOnClickListener(numberListener);
        btn2.setOnClickListener(numberListener);
        btn3.setOnClickListener(numberListener);
        btn4.setOnClickListener(numberListener);
        btn5.setOnClickListener(numberListener);
        btn6.setOnClickListener(numberListener);
        btn7.setOnClickListener(numberListener);
        btn8.setOnClickListener(numberListener);
        btn9.setOnClickListener(numberListener);
        btn0.setOnClickListener(numberListener);

        btnClear.setOnClickListener(v -> {
            currentAnswer = "";
            challengeQuestion.setText("");
            if (vibrator != null) vibrator.vibrate(30);
        });

        btnSubmit.setOnClickListener(v -> checkAnswer());

        snoozeButton.setOnClickListener(v -> snoozeAlarm());

        musicButton.setOnClickListener(v -> showMusicControls());
    }

    private void setupWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "ZenWake:AlarmLock");
        wakeLock.acquire(10 * 60 * 1000L);
    }

    private void setupVibrator() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void setupMediaPlayer() {
        Intent musicIntent = new Intent(this, MusicService.class);
        musicIntent.putExtra("alarm_id", alarmId);
        if (currentAlarm != null && currentAlarm.getMusicFolderPath() != null) {
            musicIntent.putExtra("folder_path", currentAlarm.getMusicFolderPath());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(musicIntent);
        } else {
            startService(musicIntent);
        }
    }

    private void updateTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        alarmTime.setText(timeFormat.format(new Date()));
    }

    private void updateWeather() {
        weatherManager.getCurrentWeather(new WeatherManager.WeatherCallback() {
            @Override
            public void onSuccess(String temperature, String condition) {
                runOnUiThread(() -> {
                    weatherInfo.setText("☀️ " + temperature + "° • " + condition);
                });
            }

            @Override
            public void onError(String error) {
                weatherInfo.setText("☀️ 72° • Sunny");
            }
        });
    }

    private void generateChallenge() {
        if (currentAlarm.getChallengeType().equals("math")) {
            generateMathChallenge();
        } else if (currentAlarm.getChallengeType().equals("shake")) {
            generateShakeChallenge();
        } else if (currentAlarm.getChallengeType().equals("memory")) {
            generateMemoryChallenge();
        }
    }

    private void generateMathChallenge() {
        int difficulty = challengeManager.getDifficultyLevel();
        Random random = new Random();

        switch (difficulty) {
            case 1: // Easy
                int num1 = random.nextInt(10) + 1;
                int num2 = random.nextInt(10) + 1;
                correctAnswer = num1 + num2;
                challengeQuestion.setText(num1 + " + " + num2 + " = ?");
                break;
            case 2: // Medium
                num1 = random.nextInt(20) + 5;
                num2 = random.nextInt(20) + 5;
                correctAnswer = num1 + num2;
                challengeQuestion.setText(num1 + " + " + num2 + " = ?");
                break;
            case 3: // Hard
                num1 = random.nextInt(30) + 10;
                num2 = random.nextInt(30) + 10;
                correctAnswer = num1 * num2 / 2;
                challengeQuestion.setText("(" + num1 + " × " + num2 + ") ÷ 2 = ?");
                break;
            case 4: // Insane
                num1 = random.nextInt(50) + 20;
                num2 = random.nextInt(50) + 20;
                int num3 = random.nextInt(10) + 1;
                correctAnswer = num1 * num2 - num3;
                challengeQuestion.setText(num1 + " × " + num2 + " - " + num3 + " = ?");
                break;
        }
    }

    private void generateShakeChallenge() {
        challengeQuestion.setText("Shake phone to wake up!");
        // For shake detection, you'd need SensorManager
        // For now, add a button to simulate
        Button fakeShakeButton = new Button(this);
        fakeShakeButton.setText("Simulate Shake");
        fakeShakeButton.setOnClickListener(v -> dismissAlarm());
        challengeCard.addView(fakeShakeButton);
    }

    private void generateMemoryChallenge() {
        challengeQuestion.setText("Memorize: 5-2-8-4");
        // Simple memory implementation
    }

    private void checkAnswer() {
        if (currentAnswer.isEmpty()) return;

        try {
            int userAnswer = Integer.parseInt(currentAnswer);
            if (userAnswer == correctAnswer) {
                vibrator.vibrate(200);
                Toast.makeText(this, "✓ Correct! Good morning!", Toast.LENGTH_SHORT).show();
                dismissAlarm();
            } else {
                vibrator.vibrate(new long[]{0, 200, 100, 200}, -1);
                currentAnswer = "";
                challengeQuestion.setText("");
                Toast.makeText(this, "✗ Wrong answer, try again", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSunriseAnimation() {
        sunriseRunnable = new Runnable() {
            @Override
            public void run() {
                sunriseProgress += 0.01f;
                if (sunriseProgress <= 1.0f) {
                    sunriseOverlay.setAlpha(sunriseProgress);
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(sunriseRunnable);
    }

    private void snoozeAlarm() {
        stopAlarmSounds();

        int snoozeMinutes = 5;
        long snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000);
        AlarmScheduler.scheduleSnooze(this, alarmId, snoozeTime);

        // Update snooze count in background
        new UpdateSnoozeTask().execute();

        Toast.makeText(this, "Snoozed for " + snoozeMinutes + " minutes", Toast.LENGTH_SHORT).show();
        finish();
    }

    private class UpdateSnoozeTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (currentAlarm != null) {
                currentAlarm.setSnoozeCount(currentAlarm.getSnoozeCount() + 1);
                database.alarmDao().update(currentAlarm);
            }
            return null;
        }
    }

    private void showMusicControls() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Music Controls");
        String[] options = {"Skip Track", "Volume Up", "Volume Down", "Stop Music"};
        builder.setItems(options, (dialog, which) -> {
            Intent musicIntent = new Intent(this, MusicService.class);
            switch (which) {
                case 0:
                    musicIntent.setAction("SKIP");
                    break;
                case 1:
                    musicIntent.setAction("VOLUME_UP");
                    break;
                case 2:
                    musicIntent.setAction("VOLUME_DOWN");
                    break;
                case 3:
                    musicIntent.setAction("STOP");
                    break;
            }
            startService(musicIntent);
        });
        builder.show();
    }

    private void dismissAlarm() {
        stopAlarmSounds();

        // Update success count in background
        new UpdateSuccessTask().execute();

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        // Schedule next occurrence if repeating in background
        if (currentAlarm != null && currentAlarm.isRepeating()) {
            new ScheduleNextTask().execute();
        }

        finish();
    }

    private class UpdateSuccessTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (currentAlarm != null) {
                currentAlarm.setSuccessCount(currentAlarm.getSuccessCount() + 1);
                currentAlarm.setLastTriggered(System.currentTimeMillis());
                database.alarmDao().update(currentAlarm);
            }
            return null;
        }
    }

    private class ScheduleNextTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (currentAlarm != null && currentAlarm.isRepeating()) {
                AlarmScheduler.scheduleNextOccurrence(AlarmRingingActivity.this, currentAlarm);
            }
            return null;
        }
    }

    private void stopAlarmSounds() {
        Intent musicIntent = new Intent(this, MusicService.class);
        stopService(musicIntent);

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sunriseRunnable);
        stopAlarmSounds();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}