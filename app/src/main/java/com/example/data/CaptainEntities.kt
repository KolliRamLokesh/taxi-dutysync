package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captain_profiles")
data class CaptainProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val platformName: String, // "Uber", "Ola", "Rapido"
    val captainIdStr: String,
    val isActive: Boolean = true,
    val phoneAssociated: String = "",
    val vehicleNumber: String = "",
    val registeredDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "trip_logs")
data class TripLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val platformName: String, // "Uber", "Ola", "Rapido"
    val startLocation: String, // Hyderabad locations
    val endLocation: String,
    val fareAmount: Double, // in Rupees (INR)
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shift_settings")
data class ShiftSettings(
    @PrimaryKey val id: Int = 1, // Singleton row
    val isOnDuty: Boolean = false,
    val activeRidePlatform: String? = null, // "Uber", "Ola", "Rapido" or null (all online)
    val dutyStartTimestamp: Long = 0L,
    val earningsToday: Double = 0.0,
    val selectedLanguage: String = "en" // "en" (English), "te" (Telugu), "hi" (Hindi)
)
