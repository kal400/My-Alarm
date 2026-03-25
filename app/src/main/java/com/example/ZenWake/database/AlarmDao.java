package com.example.ZenWake.database;

import androidx.room.*;
import com.example.ZenWake.models.Alarm;
import java.util.List;

@Dao
public interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    List<Alarm> getAllAlarms();

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    List<Alarm> getEnabledAlarms();

    @Query("SELECT * FROM alarms WHERE id = :id")
    Alarm getAlarmById(int id);

    // Use OnConflictStrategy.IGNORE to avoid constraint violations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("DELETE FROM alarms WHERE id = :id")
    void deleteById(int id);

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    void setEnabled(int id, boolean enabled);

    @Query("SELECT COUNT(*) FROM alarms WHERE isEnabled = 1")
    int getEnabledCount();

    @Query("SELECT AVG(successCount) FROM alarms")
    double getAverageSuccessRate();
}