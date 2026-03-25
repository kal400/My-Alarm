package com.example.ZenWake.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zenwake.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicBrowserActivity extends AppCompatActivity {

    private ListView musicListView;
    private ArrayAdapter<String> adapter;
    private List<String> musicNames = new ArrayList<>();
    private List<String> musicPaths = new ArrayList<>();
    private MediaPlayer previewPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_browser);

        musicListView = findViewById(R.id.musicListView);

        // Request permission for Android 13+ to read media
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    android.Manifest.permission.READ_MEDIA_AUDIO
            }, 100);
        }

        // Scan for music files
        scanMusicFiles();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, musicNames);
        musicListView.setAdapter(adapter);

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < musicPaths.size()) {
                    String filePath = musicPaths.get(position);
                    String fileName = musicNames.get(position);

                    // Play preview
                    playPreview(filePath);

                    // Return result
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("folder_path", filePath);
                    resultIntent.putExtra("folder_name", fileName);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(MusicBrowserActivity.this, "No music files found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (musicNames.isEmpty()) {
            Toast.makeText(this, "No music files found on device", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Found " + musicNames.size() + " songs", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanMusicFiles() {
        musicNames.clear();
        musicPaths.clear();

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        // Only get files longer than 5 seconds and less than 10 minutes (good for alarms)
        String selection = MediaStore.Audio.Media.DURATION + " >= ? AND " +
                MediaStore.Audio.Media.DURATION + " <= ?";
        String[] selectionArgs = new String[]{
                String.valueOf(5 * 1000),      // Min 5 seconds
                String.valueOf(10 * 60 * 1000) // Max 10 minutes
        };

        try (var cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.DISPLAY_NAME)) {

            if (cursor != null) {
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameColumn);
                    String path = cursor.getString(pathColumn);

                    // Only add if file exists
                    if (path != null && new File(path).exists()) {
                        musicNames.add(name);
                        musicPaths.add(path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error scanning music: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (musicNames.isEmpty()) {
            // Add a placeholder message
            musicNames.add("No music files found");
            musicPaths.add("");
        }
    }

    private void playPreview(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        // Stop any existing preview
        if (previewPlayer != null) {
            try {
                if (previewPlayer.isPlaying()) {
                    previewPlayer.stop();
                }
                previewPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            previewPlayer = null;
        }

        try {
            previewPlayer = new MediaPlayer();
            previewPlayer.setDataSource(filePath);
            previewPlayer.prepare();
            previewPlayer.start();
            previewPlayer.setVolume(0.5f, 0.5f);

            Toast.makeText(this, "Playing preview...", Toast.LENGTH_SHORT).show();

            // Stop after 5 seconds
            previewPlayer.setOnCompletionListener(mp -> {
                if (previewPlayer != null) {
                    previewPlayer.release();
                    previewPlayer = null;
                }
                Toast.makeText(this, "Preview ended", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot play this file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previewPlayer != null) {
            try {
                if (previewPlayer.isPlaying()) {
                    previewPlayer.stop();
                }
                previewPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            previewPlayer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // Re-scan after permission granted
            scanMusicFiles();
            adapter.notifyDataSetChanged();
            if (musicNames.isEmpty()) {
                Toast.makeText(this, "No music files found. Add music to your device.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Found " + musicNames.size() + " songs", Toast.LENGTH_SHORT).show();
            }
        }
    }
}