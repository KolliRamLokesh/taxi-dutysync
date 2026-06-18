package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptainDao {

    // Captain Profiles Queries
    @Query("SELECT * FROM captain_profiles ORDER BY platformName ASC")
    fun getAllProfilesFlow(): Flow<List<CaptainProfile>>

    @Query("SELECT * FROM captain_profiles WHERE platformName = :platform LIMIT 1")
    suspend fun getProfileForPlatform(platform: String): CaptainProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CaptainProfile)

    @Delete
    suspend fun deleteProfile(profile: CaptainProfile)

    @Query("DELETE FROM captain_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)


    // Trip Logs Queries
    @Query("SELECT * FROM trip_logs ORDER BY timestamp DESC")
    fun getAllTripsFlow(): Flow<List<TripLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripLog)

    @Query("DELETE FROM trip_logs WHERE id = :id")
    suspend fun deleteTripById(id: Int)


    // Shift Settings Query (Singleton Row with id = 1)
    @Query("SELECT * FROM shift_settings WHERE id = 1")
    fun getShiftSettingsFlow(): Flow<ShiftSettings?>

    @Query("SELECT * FROM shift_settings WHERE id = 1")
    suspend fun getShiftSettingsDirect(): ShiftSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShiftSettings(settings: ShiftSettings)
}
