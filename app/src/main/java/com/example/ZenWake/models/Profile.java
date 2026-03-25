package com.example.ZenWake.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profiles")
public class Profile {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String type; // "work", "weekend", "travel", "gym", "exam", "custom"
    private int defaultHour;
    private int defaultMinute;
    private String challengeType;
    private String difficulty;
    private String musicFolderPath;
    private boolean guardianAngelEnabled;
    private boolean calendarSyncEnabled;
    private boolean weatherEnabled;
    private String colorTheme;
    private boolean isPublic;
    private int downloads;
    private float rating;
    private String createdBy;
    private long createdAt;

    public Profile() {
        createdAt = System.currentTimeMillis();
        downloads = 0;
        rating = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDefaultHour() { return defaultHour; }
    public void setDefaultHour(int defaultHour) { this.defaultHour = defaultHour; }

    public int getDefaultMinute() { return defaultMinute; }
    public void setDefaultMinute(int defaultMinute) { this.defaultMinute = defaultMinute; }

    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getMusicFolderPath() { return musicFolderPath; }
    public void setMusicFolderPath(String musicFolderPath) { this.musicFolderPath = musicFolderPath; }

    public boolean isGuardianAngelEnabled() { return guardianAngelEnabled; }
    public void setGuardianAngelEnabled(boolean guardianAngelEnabled) { this.guardianAngelEnabled = guardianAngelEnabled; }

    public boolean isCalendarSyncEnabled() { return calendarSyncEnabled; }
    public void setCalendarSyncEnabled(boolean calendarSyncEnabled) { this.calendarSyncEnabled = calendarSyncEnabled; }

    public boolean isWeatherEnabled() { return weatherEnabled; }
    public void setWeatherEnabled(boolean weatherEnabled) { this.weatherEnabled = weatherEnabled; }

    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String colorTheme) { this.colorTheme = colorTheme; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public int getDownloads() { return downloads; }
    public void setDownloads(int downloads) { this.downloads = downloads; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}