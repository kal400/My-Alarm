package com.example.ZenWake.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_data")
public class SleepData {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long date;
    private int sleepQuality;
    private int deepSleep;
    private int remSleep;
    private int lightSleep;
    private long bedtime;
    private long wakeTime;

    // Constructors
    public SleepData() {
        this.date = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public int getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(int sleepQuality) { this.sleepQuality = sleepQuality; }

    public int getDeepSleep() { return deepSleep; }
    public void setDeepSleep(int deepSleep) { this.deepSleep = deepSleep; }

    public int getRemSleep() { return remSleep; }
    public void setRemSleep(int remSleep) { this.remSleep = remSleep; }

    public int getLightSleep() { return lightSleep; }
    public void setLightSleep(int lightSleep) { this.lightSleep = lightSleep; }

    public long getBedtime() { return bedtime; }
    public void setBedtime(long bedtime) { this.bedtime = bedtime; }

    public long getWakeTime() { return wakeTime; }
    public void setWakeTime(long wakeTime) { this.wakeTime = wakeTime; }
}