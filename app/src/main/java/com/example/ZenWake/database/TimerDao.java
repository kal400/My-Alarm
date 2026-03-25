package com.example.ZenWake.database;

import androidx.room.*;
import com.example.ZenWake.models.Timer;
import java.util.List;

@Dao
public interface TimerDao {

    @Query("SELECT * FROM timers ORDER BY completedAt DESC")
    List<Timer> getAllTimers();

    @Query("SELECT * FROM timers WHERE status = :status")
    List<Timer> getTimersByStatus(String status);

    @Query("SELECT * FROM timers WHERE completedAt BETWEEN :start AND :end")
    List<Timer> getTimersBetween(long start, long end);

    @Insert
    long insert(Timer timer);

    @Update
    void update(Timer timer);

    @Delete
    void delete(Timer timer);

    @Query("SELECT COUNT(*) FROM timers WHERE status = 'Completed'")
    int getCompletedCount();

    @Query("SELECT AVG(duration) FROM timers WHERE status = 'Completed'")
    double getAverageDuration();
}