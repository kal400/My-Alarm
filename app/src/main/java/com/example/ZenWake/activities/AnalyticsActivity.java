package com.example.ZenWake.activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.zenwake.R;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Alarm;
import java.util.Calendar;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView weeklyScore, weeklyScoreChange, progressDots;
    private View chartView;
    private LinearLayout challengeStatsContainer;
    private TextView guardianActivated, guardianLateness;
    private TextView timerPunctuality, timerTotal;
    private Button btnViewAllStats;

    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        database = AppDatabase.getInstance(this);

        initViews();
        setupClickListeners();

        // Load data in background
        new LoadAnalyticsTask().execute();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        weeklyScore = findViewById(R.id.weeklyScore);
        weeklyScoreChange = findViewById(R.id.weeklyScoreChange);
        progressDots = findViewById(R.id.progressDots);
        chartView = findViewById(R.id.chartView);
        challengeStatsContainer = findViewById(R.id.challengeStatsContainer);
        guardianActivated = findViewById(R.id.guardianActivated);
        guardianLateness = findViewById(R.id.guardianLateness);
        timerPunctuality = findViewById(R.id.timerPunctuality);
        timerTotal = findViewById(R.id.timerTotal);
        btnViewAllStats = findViewById(R.id.btnViewAllStats);
    }

    private class LoadAnalyticsTask extends AsyncTask<Void, Void, List<Alarm>> {

        @Override
        protected List<Alarm> doInBackground(Void... voids) {
            // This runs on background thread
            return database.alarmDao().getAllAlarms();
        }

        @Override
        protected void onPostExecute(List<Alarm> alarms) {
            // This runs on UI thread
            loadAnalytics(alarms);
        }
    }

    private void loadAnalytics(List<Alarm> allAlarms) {
        calculateWeeklyScore(allAlarms);
        loadChallengeStats();
        loadGuardianStats(allAlarms);
        loadTimerStats();
    }

    private void calculateWeeklyScore(List<Alarm> allAlarms) {
        // Calculate success rate for last 7 days
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long oneWeekAgo = calendar.getTimeInMillis();

        int totalAlarms = 0;
        int successfulAlarms = 0;

        for (Alarm alarm : allAlarms) {
            if (alarm.getLastTriggered() > oneWeekAgo) {
                totalAlarms++;
                if (alarm.getSuccessCount() > 0) {
                    successfulAlarms++;
                }
            }
        }

        int score = totalAlarms > 0 ? (successfulAlarms * 100 / totalAlarms) : 0;
        weeklyScore.setText(String.valueOf(score));

        // Calculate change from previous week
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long twoWeeksAgo = calendar.getTimeInMillis();

        int previousTotal = 0;
        int previousSuccessful = 0;

        for (Alarm alarm : allAlarms) {
            if (alarm.getLastTriggered() > twoWeeksAgo && alarm.getLastTriggered() <= oneWeekAgo) {
                previousTotal++;
                if (alarm.getSuccessCount() > 0) {
                    previousSuccessful++;
                }
            }
        }

        int previousScore = previousTotal > 0 ? (previousSuccessful * 100 / previousTotal) : 0;
        int change = score - previousScore;

        if (change >= 0) {
            weeklyScoreChange.setText("▲ " + change + "% from last week");
            weeklyScoreChange.setTextColor(Color.GREEN);
        } else {
            weeklyScoreChange.setText("▼ " + Math.abs(change) + "% from last week");
            weeklyScoreChange.setTextColor(Color.RED);
        }

        // Set progress dots
        int successfulDays = 0;
        calendar = Calendar.getInstance();
        for (int i = 0; i < 10; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            long dayStart = getStartOfDay(calendar.getTimeInMillis());
            long dayEnd = dayStart + (24 * 60 * 60 * 1000);

            boolean succeeded = false;
            for (Alarm alarm : allAlarms) {
                if (alarm.getLastTriggered() >= dayStart && alarm.getLastTriggered() <= dayEnd) {
                    succeeded = true;
                    break;
                }
            }
            if (succeeded) successfulDays++;
        }

        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < successfulDays; i++) {
            dots.append("●");
        }
        for (int i = successfulDays; i < 10; i++) {
            dots.append("○");
        }
        progressDots.setText(dots.toString());
        progressDots.setTextColor(getColor(R.color.primary_orange));
    }

    private long getStartOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void loadChallengeStats() {
        // Clear existing views
        challengeStatsContainer.removeAllViews();

        // Math stats
        addChallengeStat("Math", 92, "12s avg");

        // Shake stats
        addChallengeStat("Shake", 78, "18 shakes");

        // Memory stats
        addChallengeStat("Memory", 85, "4 steps");
    }

    private void addChallengeStat(String name, int percent, String detail) {
        LinearLayout statRow = new LinearLayout(this);
        statRow.setOrientation(LinearLayout.VERTICAL);
        statRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView nameText = new TextView(this);
        nameText.setText(name + ":");
        nameText.setTextColor(getColor(R.color.text_secondary));
        nameText.setTextSize(16);
        nameText.setLayoutParams(new LinearLayout.LayoutParams(
                180,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                8,
                1));
        progressBar.setProgressDrawable(getDrawable(R.drawable.progress_bar));
        progressBar.setProgress(percent);

        TextView percentText = new TextView(this);
        percentText.setText(percent + "%");
        percentText.setTextColor(getColor(R.color.text_light));
        percentText.setTextSize(16);
        percentText.setPadding(16, 0, 0, 0);

        topRow.addView(nameText);
        topRow.addView(progressBar);
        topRow.addView(percentText);

        TextView detailText = new TextView(this);
        detailText.setText(detail);
        detailText.setTextColor(getColor(R.color.text_secondary));
        detailText.setTextSize(14);
        detailText.setPadding(180, 4, 0, 16);

        statRow.addView(topRow);
        statRow.addView(detailText);

        challengeStatsContainer.addView(statRow);
    }

    private void loadGuardianStats(List<Alarm> allAlarms) {
        int activated = 0;
        int totalLateness = 0;
        int count = 0;

        for (Alarm alarm : allAlarms) {
            if (alarm.getFailureCount() > 0) {
                activated += alarm.getFailureCount();
                totalLateness += 12; // Placeholder
                count++;
            }
        }

        int avgLateness = count > 0 ? totalLateness / count : 0;

        guardianActivated.setText(activated + " times this month");
        guardianLateness.setText(avgLateness + " minutes");
    }

    private void loadTimerStats() {
        // Placeholder stats
        timerPunctuality.setText("84%");
        timerTotal.setText("42 timers");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnViewAllStats.setOnClickListener(v -> {
            Toast.makeText(this, "Detailed stats coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}