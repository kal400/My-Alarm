package com.example.ZenWake.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import com.example.zenwake.R;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;
import com.example.ZenWake.utils.AlarmScheduler;
import java.util.Calendar;

public class NewAlarmActivity extends AppCompatActivity {

    private static final String TAG = "NewAlarmActivity";

    // Header
    private ImageButton btnBack;

    // Time Picker
    private TextView selectedTime;
    private TimePicker timePicker;

    // Repeat Days
    private TextView daySun, dayMon, dayTue, dayWed, dayThu, dayFri, daySat;
    private boolean[] selectedDays = new boolean[7];

    // Challenge
    private Spinner challengeSpinner, difficultySpinner;

    // Music Selection
    private CardView musicCard, noMusicCard;
    private TextView musicFolderText, musicSubtitle;
    private String selectedFolderPath = "";
    private String selectedFolderName = "LOCAL MUSIC";
    private boolean useNoMusic = false;

    // Guardian Angel
    private SwitchCompat guardianSwitch;
    private TextView guardianTime;

    // Calendar Sync
    private SwitchCompat calendarSwitch;

    // Save Button
    private Button btnSaveAlarm;

    // Database
    private AppDatabase database;
    private Alarm currentAlarm;
    private int alarmId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alarm);

        database = AppDatabase.getInstance(this);

        // Check if editing existing alarm
        if (getIntent().hasExtra("alarm_id")) {
            alarmId = getIntent().getIntExtra("alarm_id", -1);
            new LoadAlarmTask().execute(alarmId);
        }

        initViews();
        setupSpinners();
        setupClickListeners();

        if (alarmId == -1) {
            // Set default values for new alarm
            Calendar now = Calendar.getInstance();
            timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(now.get(Calendar.MINUTE));
            updateSelectedTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
            guardianTime.setText("15 min");

            // Default: select weekdays (Mon-Fri)
            boolean[] defaultDays = {false, true, true, true, true, true, false}; // Sun=0, Mon=1, etc.
            for (int i = 0; i < 7; i++) {
                selectedDays[i] = defaultDays[i];
                updateDayButtonStyle(i, defaultDays[i]);
            }
        }
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);

        // Time Picker
        selectedTime = findViewById(R.id.selectedTime);
        timePicker = findViewById(R.id.timePicker);

        // Day buttons
        daySun = findViewById(R.id.daySun);
        dayMon = findViewById(R.id.dayMon);
        dayTue = findViewById(R.id.dayTue);
        dayWed = findViewById(R.id.dayWed);
        dayThu = findViewById(R.id.dayThu);
        dayFri = findViewById(R.id.dayFri);
        daySat = findViewById(R.id.daySat);

        // Challenge
        challengeSpinner = findViewById(R.id.challengeSpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);

        // Music Selection
        musicCard = findViewById(R.id.musicCard);
        noMusicCard = findViewById(R.id.noMusicCard);
        musicFolderText = findViewById(R.id.musicFolderText);
        musicSubtitle = findViewById(R.id.musicSubtitle);

        // Guardian Angel
        guardianSwitch = findViewById(R.id.guardianSwitch);
        guardianTime = findViewById(R.id.guardianTime);

        // Calendar Sync
        calendarSwitch = findViewById(R.id.calendarSwitch);

        // Save Button
        btnSaveAlarm = findViewById(R.id.btnSaveAlarm);

        // Configure TimePicker
        timePicker.setIs24HourView(false);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            updateSelectedTime(hourOfDay, minute);
        });
    }

    private void setupSpinners() {
        // Challenge spinner
        ArrayAdapter<CharSequence> challengeAdapter = ArrayAdapter.createFromResource(this,
                R.array.challenge_types, android.R.layout.simple_spinner_item);
        challengeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        challengeSpinner.setAdapter(challengeAdapter);

        // Difficulty spinner
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Day buttons
        daySun.setOnClickListener(v -> toggleDay(0));
        dayMon.setOnClickListener(v -> toggleDay(1));
        dayTue.setOnClickListener(v -> toggleDay(2));
        dayWed.setOnClickListener(v -> toggleDay(3));
        dayThu.setOnClickListener(v -> toggleDay(4));
        dayFri.setOnClickListener(v -> toggleDay(5));
        daySat.setOnClickListener(v -> toggleDay(6));

        // Music selection
        musicCard.setOnClickListener(v -> {
            useNoMusic = false;
            Intent intent = new Intent(NewAlarmActivity.this, MusicBrowserActivity.class);
            startActivityForResult(intent, 200);
        });

        // No music option
        noMusicCard.setOnClickListener(v -> {
            useNoMusic = true;
            selectedFolderPath = "";
            selectedFolderName = "NO MUSIC";
            musicFolderText.setText("NO MUSIC");
            musicSubtitle.setText("Alarm will use default system sound");
            Toast.makeText(NewAlarmActivity.this, "No music selected. Alarm will use default sound.", Toast.LENGTH_SHORT).show();
        });

        // Guardian Angel
        guardianSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            guardianTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        guardianTime.setOnClickListener(v -> showGuardianTimePicker());

        // Save button
        btnSaveAlarm.setOnClickListener(v -> saveAlarm());
    }

    private void toggleDay(int index) {
        selectedDays[index] = !selectedDays[index];
        updateDayButtonStyle(index, selectedDays[index]);
    }

    private void updateDayButtonStyle(int index, boolean selected) {
        TextView dayButton = getDayButton(index);
        if (dayButton != null) {
            if (selected) {
                dayButton.setBackgroundResource(R.drawable.day_button_selected);
                dayButton.setTextColor(getColor(R.color.text_light));
            } else {
                dayButton.setBackgroundResource(R.drawable.day_button_unselected);
                dayButton.setTextColor(getColor(R.color.text_secondary));
            }
        }
    }

    private TextView getDayButton(int index) {
        switch (index) {
            case 0: return daySun;
            case 1: return dayMon;
            case 2: return dayTue;
            case 3: return dayWed;
            case 4: return dayThu;
            case 5: return dayFri;
            case 6: return daySat;
            default: return null;
        }
    }

    private void updateSelectedTime(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour > 12 ? hour - 12 : hour;
        if (displayHour == 0) displayHour = 12;
        selectedTime.setText(String.format("%d:%02d %s", displayHour, minute, amPm));
    }

    private void showGuardianTimePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guardian Angel Delay");

        String[] times = {"5 min", "10 min", "15 min", "20 min", "30 min"};
        builder.setItems(times, (dialog, which) -> {
            guardianTime.setText(times[which]);
        });

        builder.show();
    }

    private class LoadAlarmTask extends AsyncTask<Integer, Void, Alarm> {
        @Override
        protected Alarm doInBackground(Integer... params) {
            return database.alarmDao().getAlarmById(params[0]);
        }

        @Override
        protected void onPostExecute(Alarm alarm) {
            currentAlarm = alarm;
            loadAlarmData();
        }
    }

    private void loadAlarmData() {
        if (currentAlarm != null) {
            // Time
            timePicker.setHour(currentAlarm.getHour());
            timePicker.setMinute(currentAlarm.getMinute());
            updateSelectedTime(currentAlarm.getHour(), currentAlarm.getMinute());

            // Repeating days
            boolean[] days = currentAlarm.getRepeatingDays();
            for (int i = 0; i < 7; i++) {
                selectedDays[i] = days[i];
                updateDayButtonStyle(i, days[i]);
            }

            // Challenge type
            String challenge = currentAlarm.getChallengeType();
            String[] challenges = getResources().getStringArray(R.array.challenge_types);
            for (int i = 0; i < challenges.length; i++) {
                if (challenges[i].toLowerCase().contains(challenge)) {
                    challengeSpinner.setSelection(i);
                    break;
                }
            }

            // Difficulty
            String difficulty = currentAlarm.getDifficulty();
            String[] difficulties = getResources().getStringArray(R.array.difficulty_levels);
            for (int i = 0; i < difficulties.length; i++) {
                if (difficulties[i].equalsIgnoreCase(difficulty)) {
                    difficultySpinner.setSelection(i);
                    break;
                }
            }

            // Guardian Angel
            guardianSwitch.setChecked(currentAlarm.isGuardianAngelEnabled());
            guardianTime.setText(currentAlarm.getGuardianAngelDelay() + " min");

            // Calendar Sync
            calendarSwitch.setChecked(currentAlarm.isCalendarSyncEnabled());

            // Music
            if (currentAlarm.getMusicFolderPath() != null && !currentAlarm.getMusicFolderPath().isEmpty()) {
                selectedFolderPath = currentAlarm.getMusicFolderPath();
                selectedFolderName = "CUSTOM MUSIC";
                musicFolderText.setText("MUSIC SELECTED");
                musicSubtitle.setText(selectedFolderPath.substring(selectedFolderPath.lastIndexOf("/") + 1));
                useNoMusic = false;
            } else {
                useNoMusic = true;
                selectedFolderPath = "";
                selectedFolderName = "NO MUSIC";
                musicFolderText.setText("NO MUSIC");
                musicSubtitle.setText("Alarm will use default system sound");
            }
        }
    }

    private void saveAlarm() {
        // Validate input
        if (!validateAlarm()) {
            return;
        }

        // Disable button to prevent multiple clicks
        btnSaveAlarm.setEnabled(false);
        btnSaveAlarm.setText("SAVING...");

        if (currentAlarm == null) {
            currentAlarm = new Alarm();
        }

        // Set time
        currentAlarm.setHour(timePicker.getHour());
        currentAlarm.setMinute(timePicker.getMinute());
        currentAlarm.setRepeatingDays(selectedDays);

        // Set challenge type
        String selectedChallenge = challengeSpinner.getSelectedItem().toString();
        if (selectedChallenge.contains("Math")) currentAlarm.setChallengeType("math");
        else if (selectedChallenge.contains("Shake")) currentAlarm.setChallengeType("shake");
        else if (selectedChallenge.contains("Memory")) currentAlarm.setChallengeType("memory");
        else currentAlarm.setChallengeType("math");

        // Set difficulty
        String selectedDifficulty = difficultySpinner.getSelectedItem().toString();
        currentAlarm.setDifficulty(selectedDifficulty.toLowerCase());

        // Set Guardian Angel
        currentAlarm.setGuardianAngelEnabled(guardianSwitch.isChecked());
        String guardianTimeText = guardianTime.getText().toString();
        int delay = Integer.parseInt(guardianTimeText.replace(" min", ""));
        currentAlarm.setGuardianAngelDelay(delay);

        // Set Calendar Sync
        currentAlarm.setCalendarSyncEnabled(calendarSwitch.isChecked());

        // Set Music
        if (useNoMusic || selectedFolderPath.isEmpty()) {
            currentAlarm.setMusicFolderPath(null);
        } else {
            currentAlarm.setMusicFolderPath(selectedFolderPath);
        }

        // Save in background
        new SaveAlarmTask().execute(currentAlarm);
    }

    private boolean validateAlarm() {
        // Check if at least one day is selected
        boolean hasDaySelected = false;
        for (boolean day : selectedDays) {
            if (day) {
                hasDaySelected = true;
                break;
            }
        }

        if (!hasDaySelected && alarmId == -1) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private class SaveAlarmTask extends AsyncTask<Alarm, Void, Boolean> {

        private long insertedId = -1;

        @Override
        protected Boolean doInBackground(Alarm... params) {
            try {
                Alarm alarm = params[0];

                if (alarmId == -1) {
                    // Insert new alarm
                    alarm.setId(0);
                    insertedId = database.alarmDao().insert(alarm);
                    alarm.setId((int) insertedId);
                    Log.d(TAG, "Inserted alarm with ID: " + insertedId);
                } else {
                    // Update existing alarm
                    database.alarmDao().update(alarm);
                    Log.d(TAG, "Updated alarm with ID: " + alarm.getId());
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error saving alarm: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // Re-enable button
            btnSaveAlarm.setEnabled(true);
            btnSaveAlarm.setText("SAVE ALARM");

            if (success) {
                // Schedule the alarm on the main thread
                if (currentAlarm.isEnabled()) {
                    AlarmScheduler.scheduleAlarm(NewAlarmActivity.this, currentAlarm);
                }

                Toast.makeText(NewAlarmActivity.this, "Alarm saved successfully", Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(NewAlarmActivity.this, "Error saving alarm", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            selectedFolderPath = data.getStringExtra("folder_path");
            selectedFolderName = data.getStringExtra("folder_name");
            if (selectedFolderPath != null && !selectedFolderPath.isEmpty()) {
                useNoMusic = false;
                musicFolderText.setText(selectedFolderName);
                musicSubtitle.setText("Selected: " + selectedFolderName);
                Toast.makeText(this, "Music selected: " + selectedFolderName, Toast.LENGTH_SHORT).show();
            }
        }
    }
}