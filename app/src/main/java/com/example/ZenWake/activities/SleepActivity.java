package com.example.ZenWake.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zenwake.R;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.SleepData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SleepActivity extends AppCompatActivity {

    private TextView sleepQualityText, deepSleepText, lightSleepText;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        database = AppDatabase.getInstance(this);

        sleepQualityText = findViewById(R.id.sleepQualityText);
        deepSleepText = findViewById(R.id.deepSleepText);
        lightSleepText = findViewById(R.id.lightSleepText);

        loadSleepData();
    }

    private void loadSleepData() {
        // This is a placeholder - you'd implement actual sleep tracking here
        sleepQualityText.setText("85%");
        deepSleepText.setText("2h 30m");
        lightSleepText.setText("4h 15m");
    }
}