package com.example.ZenWake.activities;

import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zenwake.R;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Timer;
import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    private EditText hoursInput, minutesInput, secondsInput;
    private Button startButton, pauseButton, resetButton;
    private TextView timerDisplay;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean timerRunning;
    private boolean timerPaused;
    private long initialTimeInMillis;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        database = AppDatabase.getInstance(this);

        initViews();
        setupClickListeners();

        // Initialize media player for timer finish sound
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void initViews() {
        hoursInput = findViewById(R.id.hoursInput);
        minutesInput = findViewById(R.id.minutesInput);
        secondsInput = findViewById(R.id.secondsInput);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        resetButton = findViewById(R.id.resetButton);
        timerDisplay = findViewById(R.id.timerDisplay);
    }

    private void setupClickListeners() {
        startButton.setOnClickListener(v -> startTimer());
        pauseButton.setOnClickListener(v -> pauseTimer());
        resetButton.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        if (timerRunning && !timerPaused) {
            return;
        }

        if (timerPaused) {
            // Resume paused timer
            resumeTimer();
            return;
        }

        // Get time from inputs
        int hours = getInputValue(hoursInput);
        int minutes = getInputValue(minutesInput);
        int seconds = getInputValue(secondsInput);

        // Calculate total milliseconds
        initialTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

        if (initialTimeInMillis <= 0) {
            Toast.makeText(this, "Please enter a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        timeLeftInMillis = initialTimeInMillis;

        startCountDown();

        // Disable input fields
        setInputsEnabled(false);
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();

                // Vibrate for last 10 seconds
                if (millisUntilFinished <= 10000 && millisUntilFinished > 0) {
                    if (vibrator != null && vibrator.hasVibrator()) {
                        // Vibrate every second
                        if (millisUntilFinished % 1000 == 0) {
                            vibrator.vibrate(200);
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                timerPaused = false;
                timerDisplay.setText("00:00:00");
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resetButton.setEnabled(true);
                setInputsEnabled(true);

                // Play sound
                playFinishSound();

                // Vibrate
                if (vibrator != null && vibrator.hasVibrator()) {
                    long[] pattern = {0, 500, 500, 500, 500, 500};
                    vibrator.vibrate(pattern, -1);
                }

                // Save timer history
                saveTimerToDatabase();

                Toast.makeText(TimerActivity.this, "Timer finished!", Toast.LENGTH_LONG).show();
            }
        }.start();

        timerRunning = true;
        timerPaused = false;
    }

    private void resumeTimer() {
        startCountDown();
        timerPaused = false;
        pauseButton.setText("Pause");
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            timerRunning = false;
            timerPaused = true;
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            startButton.setText("Resume");
        }
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timerRunning = false;
        timerPaused = false;
        timeLeftInMillis = 0;
        updateTimerDisplay();

        // Clear input fields
        hoursInput.setText("");
        minutesInput.setText("");
        secondsInput.setText("");

        // Enable inputs and reset buttons
        setInputsEnabled(true);
        startButton.setEnabled(true);
        startButton.setText("Start");
        pauseButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void updateTimerDisplay() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        timerDisplay.setText(timeFormatted);
    }

    private void playFinishSound() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // Fallback to beep
            ToneGenerator toneGen = new ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100);
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
        }
    }

    private void saveTimerToDatabase() {
        // Save timer to database in background
        new Thread(() -> {
            Timer timer = new Timer();
            timer.setDuration(initialTimeInMillis / 1000); // Convert to seconds
            timer.setCompletedAt(System.currentTimeMillis());
            timer.setStatus("Completed");
            database.timerDao().insert(timer);
        }).start();
    }

    private int getInputValue(EditText editText) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setInputsEnabled(boolean enabled) {
        hoursInput.setEnabled(enabled);
        minutesInput.setEnabled(enabled);
        secondsInput.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}