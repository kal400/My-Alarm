package com.example.ZenWake.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.ZenWake.models.Alarm;
import com.example.ZenWake.models.Profile;
import com.example.ZenWake.models.SleepData;
import com.example.ZenWake.models.Timer;  // Add this import

@Database(entities = {Alarm.class, Profile.class, SleepData.class, Timer.class},
        version = 2,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract AlarmDao alarmDao();
    public abstract ProfileDao profileDao();
    public abstract SleepDao sleepDao();
    public abstract TimerDao timerDao();  // Add this

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "zen_wake_database")
                            .fallbackToDestructiveMigration()  // Note: version changed to 2
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}