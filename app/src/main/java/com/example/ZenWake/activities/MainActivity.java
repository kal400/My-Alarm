package com.example.ZenWake.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zenwake.R;
import com.example.ZenWake.adapters.AlarmAdapter;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // TextViews
    private TextView greetingText, currentTime, currentDate, nextEvent, nextAlarmText, timerCountText, weatherText;

    // Clickable buttons
    private TextView sleepButton, statsButton, timerButton;  // Changed from profileButton to timerButton

    // RecyclerView for alarms
    private RecyclerView alarmsRecyclerView;
    private AlarmAdapter alarmAdapter;
    private List<Alarm> alarmList = new ArrayList<>();

    // Other views
    private FloatingActionButton fabNewAlarm;

    // Database
    private AppDatabase database;

    // Handler for time updates
    private Handler handler = new Handler();
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate called");

        database = AppDatabase.getInstance(this);

        initViews();
        setupClickListeners();
        updateTime();

        // Setup RecyclerView
        setupRecyclerView();

        // Load alarms
        loadAlarms();
    }

    private void initViews() {
        Log.d(TAG, "initViews called");

        // TextViews
        greetingText = findViewById(R.id.greetingText);
        currentTime = findViewById(R.id.currentTime);
        currentDate = findViewById(R.id.currentDate);
        nextEvent = findViewById(R.id.nextEvent);
        nextAlarmText = findViewById(R.id.nextAlarmText);
        timerCountText = findViewById(R.id.timerCountText);
        weatherText = findViewById(R.id.weatherText);

        // Clickable buttons
        sleepButton = findViewById(R.id.sleepButton);
        statsButton = findViewById(R.id.statsButton);
        timerButton = findViewById(R.id.timerButton);  // Changed from profileButton to timerButton

        // FAB
        fabNewAlarm = findViewById(R.id.fabNewAlarm);

        // RecyclerView
        alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView);

        if (alarmsRecyclerView == null) {
            Log.e(TAG, "alarmsRecyclerView is NULL! Check your layout ID");
        } else {
            Log.d(TAG, "alarmsRecyclerView found");
        }
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView called");

        if (alarmsRecyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            alarmsRecyclerView.setLayoutManager(layoutManager);

            alarmAdapter = new AlarmAdapter(alarmList, new AlarmAdapter.OnAlarmClickListener() {
                @Override
                public void onAlarmClick(Alarm alarm) {
                    Log.d(TAG, "Alarm clicked: " + alarm.getId());
                    Intent intent = new Intent(MainActivity.this, NewAlarmActivity.class);
                    intent.putExtra("alarm_id", alarm.getId());
                    startActivity(intent);
                }

                @Override
                public void onToggleAlarm(Alarm alarm, boolean isEnabled) {
                    Log.d(TAG, "Alarm toggled: " + alarm.getId() + " to " + isEnabled);
                    alarm.setEnabled(isEnabled);
                    updateAlarmInBackground(alarm);
                }

                @Override
                public void onDeleteAlarm(Alarm alarm) {
                    Log.d(TAG, "Alarm deleted: " + alarm.getId());
                    deleteAlarmInBackground(alarm);
                }
            });

            alarmsRecyclerView.setAdapter(alarmAdapter);
            Log.d(TAG, "AlarmAdapter set on RecyclerView");
        } else {
            Log.e(TAG, "Cannot setup RecyclerView - it's null");
        }
    }

    private void setupClickListeners() {
        if (sleepButton != null) {
            sleepButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SleepActivity.class));
            });
        }

        if (statsButton != null) {
            statsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AnalyticsActivity.class));
            });
        }

        // Timer button - opens TimerActivity
        if (timerButton != null) {
            timerButton.setOnClickListener(v -> {
                Log.d(TAG, "Timer button clicked");
                startActivity(new Intent(MainActivity.this, TimerActivity.class));
            });
        }

        if (fabNewAlarm != null) {
            fabNewAlarm.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, NewAlarmActivity.class);
                startActivity(intent);
            });
        }
    }

    private void updateTime() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                SimpleDateFormat hourFormat = new SimpleDateFormat("H", Locale.getDefault());

                Date now = new Date();
                String time = timeFormat.format(now);
                String date = dateFormat.format(now).toUpperCase();
                int hour = Integer.parseInt(hourFormat.format(now));

                if (currentTime != null) currentTime.setText(time);
                if (currentDate != null) currentDate.setText(date);

                // Update greeting based on time
                if (greetingText != null) {
                    if (hour < 12) {
                        greetingText.setText("GOOD MORNING");
                    } else if (hour < 17) {
                        greetingText.setText("GOOD AFTERNOON");
                    } else {
                        greetingText.setText("GOOD EVENING");
                    }
                }

                handler.postDelayed(this, 60000);
            }
        };
        handler.post(timeRunnable);
    }

    private void loadAlarms() {
        Log.d(TAG, "loadAlarms called");
        new LoadAlarmsTask().execute();
    }

    private void updateAlarmInBackground(Alarm alarm) {
        new UpdateAlarmTask().execute(alarm);
    }

    private void deleteAlarmInBackground(Alarm alarm) {
        new DeleteAlarmTask().execute(alarm);
    }

    private class LoadAlarmsTask extends AsyncTask<Void, Void, List<Alarm>> {
        @Override
        protected List<Alarm> doInBackground(Void... voids) {
            Log.d(TAG, "LoadAlarmsTask doInBackground");
            List<Alarm> alarms = database.alarmDao().getAllAlarms();
            Log.d(TAG, "Loaded " + (alarms != null ? alarms.size() : 0) + " alarms from database");

            if (alarms != null) {
                for (Alarm a : alarms) {
                    Log.d(TAG, "Alarm in DB: ID=" + a.getId() +
                            ", Time=" + a.getHour() + ":" + a.getMinute() +
                            ", Enabled=" + a.isEnabled());
                }
            }
            return alarms;
        }

        @Override
        protected void onPostExecute(List<Alarm> alarms) {
            Log.d(TAG, "LoadAlarmsTask onPostExecute");

            alarmList.clear();
            if (alarms != null && !alarms.isEmpty()) {
                alarmList.addAll(alarms);
                Log.d(TAG, "Added " + alarms.size() + " alarms to list");
            } else {
                Log.d(TAG, "No alarms in database");
            }

            if (alarmAdapter != null) {
                alarmAdapter.notifyDataSetChanged();
                Log.d(TAG, "Adapter notified of data change");
                Log.d(TAG, "Adapter now has " + alarmAdapter.getItemCount() + " items");
            } else {
                Log.e(TAG, "alarmAdapter is null!");
            }

            // Update next alarm text
            updateNextAlarmText();
        }
    }

    private class UpdateAlarmTask extends AsyncTask<Alarm, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Alarm... alarms) {
            try {
                database.alarmDao().update(alarms[0]);
                Log.d(TAG, "Alarm updated in database: " + alarms[0].getId());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                loadAlarms();
            } else {
                Toast.makeText(MainActivity.this, "Error updating alarm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteAlarmTask extends AsyncTask<Alarm, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Alarm... alarms) {
            try {
                database.alarmDao().delete(alarms[0]);
                Log.d(TAG, "Alarm deleted from database: " + alarms[0].getId());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                loadAlarms();
            } else {
                Toast.makeText(MainActivity.this, "Error deleting alarm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNextAlarmText() {
        Log.d(TAG, "updateNextAlarmText called");

        if (nextAlarmText != null) {
            if (alarmList.isEmpty()) {
                nextAlarmText.setText("⏰ NO ALARMS SET");
                Log.d(TAG, "No alarms, text set to 'NO ALARMS SET'");
            } else {
                Alarm firstAlarm = alarmList.get(0);
                String nextTime = firstAlarm.getFormattedTime();
                nextAlarmText.setText("⏰ NEXT: " + nextTime);
                Log.d(TAG, "Next alarm text set to: " + nextTime);
            }
        } else {
            Log.e(TAG, "nextAlarmText is null!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        updateTime();
        loadAlarms();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(timeRunnable);
    }
}