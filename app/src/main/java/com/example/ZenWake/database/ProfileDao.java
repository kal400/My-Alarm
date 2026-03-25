package com.example.ZenWake.database;

import androidx.room.*;
import com.example.ZenWake.models.Profile;
import java.util.List;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM profiles")
    List<Profile> getAllProfiles();

    @Query("SELECT * FROM profiles WHERE id = :id")
    Profile getProfileById(int id);

    @Query("SELECT * FROM profiles WHERE type = :type")
    List<Profile> getProfilesByType(String type);

    @Insert
    long insert(Profile profile);

    @Update
    void update(Profile profile);

    @Delete
    void delete(Profile profile);

    @Query("SELECT * FROM profiles WHERE isPublic = 1 ORDER BY downloads DESC")
    List<Profile> getPublicProfiles();
}