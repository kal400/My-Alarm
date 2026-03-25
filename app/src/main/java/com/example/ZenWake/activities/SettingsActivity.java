package com.example.ZenWake.activities;

import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zenwake.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch darkModeSwitch, notificationsSwitch, vibrationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        vibrationSwitch = findViewById(R.id.vibrationSwitch);

        // Load saved preferences
        loadSettings();
    }

    private void loadSettings() {
        // Load from SharedPreferences
        darkModeSwitch.setChecked(false);
        notificationsSwitch.setChecked(true);
        vibrationSwitch.setChecked(true);
    }
}