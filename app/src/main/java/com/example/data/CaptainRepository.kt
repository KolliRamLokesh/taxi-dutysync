package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CaptainRepository(private val dao: CaptainDao) {

    val allProfiles: Flow<List<CaptainProfile>> = dao.getAllProfilesFlow()
    val allTrips: Flow<List<TripLog>> = dao.getAllTripsFlow()
    val shiftSettings: Flow<ShiftSettings?> = dao.getShiftSettingsFlow()

    suspend fun getProfileForPlatform(platform: String): CaptainProfile? {
        return dao.getProfileForPlatform(platform)
    }

    suspend fun saveProfile(profile: CaptainProfile) {
        dao.insertProfile(profile)
    }

    suspend fun deleteProfileById(id: Int) {
        dao.deleteProfileById(id)
    }

    suspend fun addTrip(trip: TripLog) {
        dao.insertTrip(trip)
        
        // Accumulate earnings in shift settings
        val current = dao.getShiftSettingsDirect() ?: ShiftSettings()
        val newEarnings = current.earningsToday + trip.fareAmount
        dao.insertShiftSettings(current.copy(earningsToday = newEarnings))
    }

    suspend fun deleteTripById(id: Int) {
        dao.deleteTripById(id)
    }

    suspend fun updateShiftDuty(isOnDuty: Boolean) {
        val current = dao.getShiftSettingsDirect() ?: ShiftSettings()
        val startTs = if (isOnDuty) System.currentTimeMillis() else current.dutyStartTimestamp
        val earnings = if (!isOnDuty) 0.0 else current.earningsToday // reset daily earnings when going offline? Or retain them during shift. Let's retain them, but reset if starting a completely new shift
        val updated = current.copy(
            isOnDuty = isOnDuty,
            dutyStartTimestamp = startTs,
            activeRidePlatform = null, // clear active ride on duty change
            earningsToday = if (isOnDuty && current.dutyStartTimestamp == 0L) 0.0 else current.earningsToday
        )
        dao.insertShiftSettings(updated)
    }

    suspend fun updateActiveRide(platform: String?) {
        val current = dao.getShiftSettingsDirect() ?: ShiftSettings()
        val updated = current.copy(
            activeRidePlatform = platform
        )
        dao.insertShiftSettings(updated)
    }

    suspend fun updateLanguage(langCode: String) {
        val current = dao.getShiftSettingsDirect() ?: ShiftSettings()
        val updated = current.copy(selectedLanguage = langCode)
        dao.insertShiftSettings(updated)
    }

    suspend fun initializeSettings() {
        val current = dao.getShiftSettingsDirect()
        if (current == null) {
            dao.insertShiftSettings(ShiftSettings())
        }
    }
}
