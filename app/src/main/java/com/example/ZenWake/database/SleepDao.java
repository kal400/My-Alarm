package com.example.ZenWake.database;

import androidx.room.*;
import com.example.ZenWake.models.SleepData;
import java.util.List;

@Dao
public interface SleepDao {

    @Query("SELECT * FROM sleep_data ORDER BY date DESC")
    List<SleepData> getAllSleepData();

    @Query("SELECT * FROM sleep_data WHERE date BETWEEN :start AND :end")
    List<SleepData> getSleepDataBetween(long start, long end);

    @Query("SELECT * FROM sleep_data WHERE id = :id")
    SleepData getSleepDataById(int id);

    @Insert
    long insert(SleepData sleepData);

    @Update
    void update(SleepData sleepData);

    @Delete
    void delete(SleepData sleepData);

    @Query("SELECT AVG(sleepQuality) FROM sleep_data")
    double getAverageSleepQuality();

    @Query("SELECT COUNT(*) FROM sleep_data")
    int getTotalEntries();
}