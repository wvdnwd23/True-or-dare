package com.wes.truthdare.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension function to create a DataStore for preferences
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * Class for managing app preferences
 */
@Singleton
class AppPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        // Preference keys
        private val VOICE_MODE_ENABLED = booleanPreferencesKey("voice_mode_enabled")
        private val JOURNAL_ENABLED = booleanPreferencesKey("journal_enabled")
        private val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        private val VOLUME_LEVEL = intPreferencesKey("volume_level")
        private val DEFAULT_GAME_MODE = stringPreferencesKey("default_game_mode")
        private val DEFAULT_HEAT_LEVEL = intPreferencesKey("default_heat_level")
        private val DEFAULT_DEPTH_LEVEL = intPreferencesKey("default_depth_level")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val CURRENT_SESSION_ID = stringPreferencesKey("current_session_id")
        private val LAST_ACTIVE_PLAYER_ID = stringPreferencesKey("last_active_player_id")
    }
    
    /**
     * Get whether voice mode is enabled
     */
    val voiceModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[VOICE_MODE_ENABLED] ?: false
        }
    
    /**
     * Set whether voice mode is enabled
     * @param enabled Whether voice mode is enabled
     */
    suspend fun setVoiceModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VOICE_MODE_ENABLED] = enabled
        }
    }
    
    /**
     * Get whether journal is enabled
     */
    val journalEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[JOURNAL_ENABLED] ?: false
        }
    
    /**
     * Set whether journal is enabled
     * @param enabled Whether journal is enabled
     */
    suspend fun setJournalEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[JOURNAL_ENABLED] = enabled
        }
    }
    
    /**
     * Get whether haptics are enabled
     */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAPTICS_ENABLED] ?: true
        }
    
    /**
     * Set whether haptics are enabled
     * @param enabled Whether haptics are enabled
     */
    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED] = enabled
        }
    }
    
    /**
     * Get the volume level
     */
    val volumeLevel: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[VOLUME_LEVEL] ?: 50
        }
    
    /**
     * Set the volume level
     * @param level The volume level (0-100)
     */
    suspend fun setVolumeLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[VOLUME_LEVEL] = level.coerceIn(0, 100)
        }
    }
    
    /**
     * Get the default game mode
     */
    val defaultGameMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_GAME_MODE] ?: "CASUAL"
        }
    
    /**
     * Set the default game mode
     * @param mode The default game mode
     */
    suspend fun setDefaultGameMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_GAME_MODE] = mode
        }
    }
    
    /**
     * Get the default heat level
     */
    val defaultHeatLevel: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_HEAT_LEVEL] ?: 50
        }
    
    /**
     * Set the default heat level
     * @param level The default heat level (0-100)
     */
    suspend fun setDefaultHeatLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_HEAT_LEVEL] = level.coerceIn(0, 100)
        }
    }
    
    /**
     * Get the default depth level
     */
    val defaultDepthLevel: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_DEPTH_LEVEL] ?: 1
        }
    
    /**
     * Set the default depth level
     * @param level The default depth level (1-5)
     */
    suspend fun setDefaultDepthLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_DEPTH_LEVEL] = level.coerceIn(1, 5)
        }
    }
    
    /**
     * Get whether onboarding has been completed
     */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }
    
    /**
     * Set whether onboarding has been completed
     * @param completed Whether onboarding has been completed
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    /**
     * Get the current session ID
     */
    val currentSessionId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_SESSION_ID]
        }
    
    /**
     * Set the current session ID
     * @param sessionId The current session ID
     */
    suspend fun setCurrentSessionId(sessionId: String?) {
        context.dataStore.edit { preferences ->
            if (sessionId != null) {
                preferences[CURRENT_SESSION_ID] = sessionId
            } else {
                preferences.remove(CURRENT_SESSION_ID)
            }
        }
    }
    
    /**
     * Get the last active player ID
     */
    val lastActivePlayerId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_ACTIVE_PLAYER_ID]
        }
    
    /**
     * Set the last active player ID
     * @param playerId The last active player ID
     */
    suspend fun setLastActivePlayerId(playerId: String?) {
        context.dataStore.edit { preferences ->
            if (playerId != null) {
                preferences[LAST_ACTIVE_PLAYER_ID] = playerId
            } else {
                preferences.remove(LAST_ACTIVE_PLAYER_ID)
            }
        }
    }
    
    /**
     * Clear all preferences
     */
    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}