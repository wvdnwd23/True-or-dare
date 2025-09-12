package com.wes.truthdare.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Manager for session limits in drinking games
 */
@Singleton
class SessionLimitsManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val TIME_LIMIT_KEY = intPreferencesKey("session_time_limit_minutes")
        private val DRINK_LIMIT_KEY = intPreferencesKey("session_drink_limit_count")
        private val SESSION_START_TIME_KEY = longPreferencesKey("session_start_time")
    }
    
    // In-memory cache of the session limits
    private val _timeLimitMinutes = MutableStateFlow<Int?>(null)
    val timeLimitMinutes: StateFlow<Int?> = _timeLimitMinutes
    
    private val _drinkLimitCount = MutableStateFlow<Int?>(null)
    val drinkLimitCount: StateFlow<Int?> = _drinkLimitCount
    
    private val _sessionStartTime = MutableStateFlow<Long?>(null)
    
    // Remaining time in minutes
    private val _remainingTimeMinutes = MutableStateFlow<Int?>(null)
    
    // Flag indicating if the time limit has been reached
    private val _isTimeLimitReached = MutableStateFlow(false)
    
    // Job for the timer
    private var timerJob: Job? = null
    
    init {
        // Load the session limits from DataStore
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.data.map { preferences ->
                Triple(
                    preferences[TIME_LIMIT_KEY],
                    preferences[DRINK_LIMIT_KEY],
                    preferences[SESSION_START_TIME_KEY]
                )
            }.collect { (timeLimit, drinkLimit, startTime) ->
                _timeLimitMinutes.value = timeLimit
                _drinkLimitCount.value = drinkLimit
                _sessionStartTime.value = startTime
                
                // Update remaining time
                updateRemainingTime()
                
                // Start the timer if we have a time limit and start time
                if (timeLimit != null && startTime != null) {
                    startTimer()
                }
            }
        }
    }
    
    /**
     * Set the time limit in minutes
     * @param minutes The time limit in minutes, or null to clear the limit
     */
    suspend fun setTimeLimit(minutes: Int?) {
        dataStore.edit { preferences ->
            if (minutes != null && minutes > 0) {
                preferences[TIME_LIMIT_KEY] = minutes
            } else {
                preferences.remove(TIME_LIMIT_KEY)
            }
        }
        _timeLimitMinutes.value = minutes
        
        // Update remaining time
        updateRemainingTime()
        
        // Start the timer if we have a time limit and start time
        if (minutes != null && _sessionStartTime.value != null) {
            startTimer()
        } else {
            timerJob?.cancel()
            timerJob = null
        }
    }
    
    /**
     * Set the drink limit count
     * @param count The drink limit count, or null to clear the limit
     */
    suspend fun setDrinkLimit(count: Int?) {
        dataStore.edit { preferences ->
            if (count != null && count > 0) {
                preferences[DRINK_LIMIT_KEY] = count
            } else {
                preferences.remove(DRINK_LIMIT_KEY)
            }
        }
        _drinkLimitCount.value = count
    }
    
    /**
     * Clear all session limits
     */
    suspend fun clearLimits() {
        dataStore.edit { preferences ->
            preferences.remove(TIME_LIMIT_KEY)
            preferences.remove(DRINK_LIMIT_KEY)
        }
        _timeLimitMinutes.value = null
        _drinkLimitCount.value = null
        _remainingTimeMinutes.value = null
        _isTimeLimitReached.value = false
        
        timerJob?.cancel()
        timerJob = null
    }
    
    /**
     * Start a new session
     */
    suspend fun startSession() {
        val currentTime = System.currentTimeMillis()
        dataStore.edit { preferences ->
            preferences[SESSION_START_TIME_KEY] = currentTime
        }
        _sessionStartTime.value = currentTime
        
        // Update remaining time
        updateRemainingTime()
        
        // Start the timer if we have a time limit
        if (_timeLimitMinutes.value != null) {
            startTimer()
        }
    }
    
    /**
     * End the current session
     */
    suspend fun endSession() {
        dataStore.edit { preferences ->
            preferences.remove(SESSION_START_TIME_KEY)
        }
        _sessionStartTime.value = null
        _remainingTimeMinutes.value = null
        _isTimeLimitReached.value = false
        
        timerJob?.cancel()
        timerJob = null
    }
    
    /**
     * Get the remaining time in minutes
     * @return Flow of remaining time in minutes, or null if no time limit is set
     */
    fun getRemainingTimeMinutes(): StateFlow<Int?> {
        return _remainingTimeMinutes
    }
    
    /**
     * Check if the time limit has been reached
     * @return Flow of Boolean indicating if the time limit has been reached
     */
    fun isTimeLimitReached(): StateFlow<Boolean> {
        return _isTimeLimitReached
    }
    
    /**
     * Update the remaining time based on the current time and session start time
     */
    private fun updateRemainingTime() {
        val timeLimit = _timeLimitMinutes.value
        val startTime = _sessionStartTime.value
        
        if (timeLimit != null && startTime != null) {
            val currentTime = System.currentTimeMillis()
            val elapsedMinutes = (currentTime - startTime) / (60 * 1000)
            val remainingMinutes = max(0, timeLimit - elapsedMinutes.toInt())
            
            _remainingTimeMinutes.value = remainingMinutes
            _isTimeLimitReached.value = remainingMinutes <= 0
        } else {
            _remainingTimeMinutes.value = null
            _isTimeLimitReached.value = false
        }
    }
    
    /**
     * Start a timer to update the remaining time
     */
    private fun startTimer() {
        timerJob?.cancel()
        
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                // Update remaining time
                updateRemainingTime()
                
                // Check if time limit has been reached
                if (_isTimeLimitReached.value) {
                    break
                }
                
                // Wait for 1 minute
                delay(60 * 1000L)
            }
        }
    }
}