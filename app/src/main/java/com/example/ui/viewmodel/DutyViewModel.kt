package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DutyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CaptainRepository

    val allProfiles: StateFlow<List<CaptainProfile>>
    val allTrips: StateFlow<List<TripLog>>
    val shiftSettings: StateFlow<ShiftSettings>

    init {
        val database = CaptainDatabase.getDatabase(application)
        repository = CaptainRepository(database.captainDao())

        allProfiles = repository.allProfiles
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        allTrips = repository.allTrips
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        shiftSettings = repository.shiftSettings
            .map { it ?: ShiftSettings() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ShiftSettings()
            )

        viewModelScope.launch {
            repository.initializeSettings()
        }
    }

    fun addCaptainProfile(platform: String, captainId: String, vehicleNum: String, phoneNum: String) {
        viewModelScope.launch {
            val profile = CaptainProfile(
                platformName = platform,
                captainIdStr = captainId,
                vehicleNumber = vehicleNum,
                phoneAssociated = phoneNum
            )
            repository.saveProfile(profile)
        }
    }

    fun deleteProfile(id: Int) {
        viewModelScope.launch {
            repository.deleteProfileById(id)
        }
    }

    fun toggleMasterDuty(isOn: Boolean) {
        viewModelScope.launch {
            repository.updateShiftDuty(isOn)
        }
    }

    fun selectLanguage(langCode: String) {
        viewModelScope.launch {
            repository.updateLanguage(langCode)
        }
    }

    fun triggerActiveRide(platform: String) {
        viewModelScope.launch {
            repository.updateActiveRide(platform)
        }
    }

    fun completeActiveRide(fromLoc: String, toLoc: String, fare: Double, duration: Int) {
        viewModelScope.launch {
            val settings = shiftSettings.value
            val platform = settings.activeRidePlatform ?: "Uber"
            
            val trip = TripLog(
                platformName = platform,
                startLocation = fromLoc,
                endLocation = toLoc,
                fareAmount = fare,
                durationMinutes = duration
            )
            repository.addTrip(trip)
            repository.updateActiveRide(null) // Reset active app status, triggering other apps ONLINE!
        }
    }
    
    fun dismissActiveRideNoLog() {
        viewModelScope.launch {
            repository.updateActiveRide(null)
        }
    }

    fun deleteTripLog(id: Int) {
        viewModelScope.launch {
            repository.deleteTripById(id)
        }
    }
}
