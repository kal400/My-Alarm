package com.example.ZenWake.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Calendar;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int hour;
    private int minute;
    private boolean isEnabled;
    private String repeatingDaysString; // Store as "1,1,1,1,1,0,0" format
    private String challengeType;
    private String difficulty;
    private String musicFolderPath;
    private boolean guardianAngelEnabled;
    private int guardianAngelDelay;
    private boolean calendarSyncEnabled;
    private String label;
    private int snoozeCount;
    private long createdAt;
    private long lastTriggered;
    private int successCount;
    private int failureCount;

    // No-arg constructor required by Room
    public Alarm() {
        // Initialize with default values
        this.repeatingDaysString = "0,0,0,0,0,0,0"; // All days off by default
        this.isEnabled = true;
        this.challengeType = "math";
        this.difficulty = "medium";
        this.guardianAngelEnabled = true;
        this.guardianAngelDelay = 15;
        this.calendarSyncEnabled = false;
        this.snoozeCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastTriggered = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.label = "";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public String getRepeatingDaysString() { return repeatingDaysString; }
    public void setRepeatingDaysString(String repeatingDaysString) {
        this.repeatingDaysString = repeatingDaysString;
    }

    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getMusicFolderPath() { return musicFolderPath; }
    public void setMusicFolderPath(String musicFolderPath) { this.musicFolderPath = musicFolderPath; }

    public boolean isGuardianAngelEnabled() { return guardianAngelEnabled; }
    public void setGuardianAngelEnabled(boolean guardianAngelEnabled) { this.guardianAngelEnabled = guardianAngelEnabled; }

    public int getGuardianAngelDelay() { return guardianAngelDelay; }
    public void setGuardianAngelDelay(int guardianAngelDelay) { this.guardianAngelDelay = guardianAngelDelay; }

    public boolean isCalendarSyncEnabled() { return calendarSyncEnabled; }
    public void setCalendarSyncEnabled(boolean calendarSyncEnabled) { this.calendarSyncEnabled = calendarSyncEnabled; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getSnoozeCount() { return snoozeCount; }
    public void setSnoozeCount(int snoozeCount) { this.snoozeCount = snoozeCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastTriggered() { return lastTriggered; }
    public void setLastTriggered(long lastTriggered) { this.lastTriggered = lastTriggered; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    // Helper methods for working with boolean array
    public boolean[] getRepeatingDays() {
        boolean[] days = new boolean[7];
        if (repeatingDaysString != null && !repeatingDaysString.isEmpty()) {
            String[] parts = repeatingDaysString.split(",");
            for (int i = 0; i < 7 && i < parts.length; i++) {
                days[i] = parts[i].trim().equals("1");
            }
        }
        return days;
    }

    public void setRepeatingDays(boolean[] days) {
        if (days == null || days.length != 7) return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (i > 0) sb.append(",");
            sb.append(days[i] ? "1" : "0");
        }
        this.repeatingDaysString = sb.toString();
    }

    public String getFormattedTime() {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour > 12 ? hour - 12 : hour;
        if (displayHour == 0) displayHour = 12;
        return String.format("%d:%02d %s", displayHour, minute, amPm);
    }

    public boolean isRepeating() {
        boolean[] days = getRepeatingDays();
        for (boolean day : days) {
            if (day) return true;
        }
        return false;
    }

    public String getRepeatingDaysDisplay() {
        if (!isRepeating()) return "Once";

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        boolean[] days = getRepeatingDays();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            if (days[i]) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(dayNames[i]);
            }
        }
        return sb.toString();
    }
}