package com.wes.truthdare.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for session limits functionality
 */
@Singleton
class SessionLimitsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.limitsDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_limits")
    
    companion object {
        private val TIME_LIMIT_MINUTES = intPreferencesKey("time_limit_minutes")
        private val DRINK_LIMIT_COUNT = intPreferencesKey("drink_limit_count")
        private val SESSION_START_TIME = longPreferencesKey("session_start_time")
    }
    
    /**
     * Get the current time limit in minutes
     */
    val timeLimitMinutes: Flow<Int?> = context.limitsDataStore.data
        .map { preferences ->
            preferences[TIME_LIMIT_MINUTES]
        }
    
    /**
     * Get the current drink limit count
     */
    val drinkLimitCount: Flow<Int?> = context.limitsDataStore.data
        .map { preferences ->
            preferences[DRINK_LIMIT_COUNT]
        }
    
    /**
     * Get the session start time in milliseconds
     */
    val sessionStartTime: Flow<Long?> = context.limitsDataStore.data
        .map { preferences ->
            preferences[SESSION_START_TIME]
        }
    
    /**
     * Set the time limit in minutes
     * @param minutes The time limit in minutes
     */
    suspend fun setTimeLimit(minutes: Int?) {
        context.limitsDataStore.edit { preferences ->
            if (minutes != null && minutes > 0) {
                preferences[TIME_LIMIT_MINUTES] = minutes
            } else {
                preferences.remove(TIME_LIMIT_MINUTES)
            }
        }
    }
    
    /**
     * Set the drink limit count
     * @param count The drink limit count
     */
    suspend fun setDrinkLimit(count: Int?) {
        context.limitsDataStore.edit { preferences ->
            if (count != null && count > 0) {
                preferences[DRINK_LIMIT_COUNT] = count
            } else {
                preferences.remove(DRINK_LIMIT_COUNT)
            }
        }
    }
    
    /**
     * Start a new session
     */
    suspend fun startSession() {
        context.limitsDataStore.edit { preferences ->
            preferences[SESSION_START_TIME] = System.currentTimeMillis()
        }
    }
    
    /**
     * End the current session
     */
    suspend fun endSession() {
        context.limitsDataStore.edit { preferences ->
            preferences.remove(SESSION_START_TIME)
        }
    }
    
    /**
     * Check if the time limit has been reached
     * @return Flow of Boolean indicating if the time limit has been reached
     */
    fun isTimeLimitReached(): Flow<Boolean> {
        return context.limitsDataStore.data.map { preferences ->
            val startTime = preferences[SESSION_START_TIME] ?: return@map false
            val limitMinutes = preferences[TIME_LIMIT_MINUTES] ?: return@map false
            
            val currentTime = System.currentTimeMillis()
            val elapsedMinutes = (currentTime - startTime) / (60 * 1000)
            
            elapsedMinutes >= limitMinutes
        }
    }
    
    /**
     * Get the remaining time in minutes
     * @return Flow of Int representing the remaining minutes, or null if no time limit is set
     */
    fun getRemainingTimeMinutes(): Flow<Int?> {
        return context.limitsDataStore.data.map { preferences ->
            val startTime = preferences[SESSION_START_TIME] ?: return@map null
            val limitMinutes = preferences[TIME_LIMIT_MINUTES] ?: return@map null
            
            val currentTime = System.currentTimeMillis()
            val elapsedMinutes = (currentTime - startTime) / (60 * 1000)
            val remainingMinutes = limitMinutes - elapsedMinutes
            
            if (remainingMinutes > 0) remainingMinutes.toInt() else 0
        }
    }
    
    /**
     * Clear all session limits
     */
    suspend fun clearLimits() {
        context.limitsDataStore.edit { preferences ->
            preferences.remove(TIME_LIMIT_MINUTES)
            preferences.remove(DRINK_LIMIT_COUNT)
            preferences.remove(SESSION_START_TIME)
        }
    }
}