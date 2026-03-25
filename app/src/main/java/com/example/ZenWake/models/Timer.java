package com.example.ZenWake.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timers")
public class Timer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long duration; // in seconds
    private long completedAt;
    private String status; // "Completed", "Cancelled", "Interrupted"

    public Timer() {
        this.status = "Completed";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFormattedDuration() {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}