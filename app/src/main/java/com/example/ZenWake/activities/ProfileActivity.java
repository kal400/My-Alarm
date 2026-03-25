package com.example.ZenWake.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zenwake.R;
import com.example.ZenWake.database.AppDatabase;
import com.example.ZenWake.models.Profile;

public class ProfileActivity extends AppCompatActivity {

    private EditText profileNameEdit;
    private Button saveButton, cancelButton;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = AppDatabase.getInstance(this);

        profileNameEdit = findViewById(R.id.profileNameEdit);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveProfile() {
        String name = profileNameEdit.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a profile name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Save in background
        new SaveProfileTask().execute(name);
    }

    private class SaveProfileTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String name = params[0];

                Profile profile = new Profile();
                profile.setName(name);
                profile.setType("custom");
                profile.setCreatedAt(System.currentTimeMillis());

                database.profileDao().insert(profile);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // Re-enable button
            saveButton.setEnabled(true);
            saveButton.setText("Save");

            if (success) {
                Toast.makeText(ProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                finish(); // Go back after successful save
            } else {
                Toast.makeText(ProfileActivity.this, "Error saving profile", Toast.LENGTH_LONG).show();
            }
        }
    }
}