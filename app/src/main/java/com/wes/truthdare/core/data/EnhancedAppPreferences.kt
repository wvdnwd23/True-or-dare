package com.wes.truthdare.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension function to create a DataStore for enhanced preferences
 */
private val Context.enhancedDataStore: DataStore<Preferences> by preferencesDataStore(name = "enhanced_preferences")

/**
 * Enhanced app preferences for storing additional user settings
 */
@Singleton
class EnhancedAppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Preference keys
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        private val BACKGROUND_MUSIC_ENABLED = booleanPreferencesKey("background_music_enabled")
        private val CONTENT_FILTER_LEVEL = stringPreferencesKey("content_filter_level")
        private val PREMIUM_SUBSCRIPTION = booleanPreferencesKey("premium_subscription")
        private val LAST_DAILY_DARE_DATE = longPreferencesKey("last_daily_dare_date")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val LAST_APP_VERSION = intPreferencesKey("last_app_version")
        private val TOTAL_PLAY_TIME = longPreferencesKey("total_play_time")
        private val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        private val REFERRAL_CODE = stringPreferencesKey("referral_code")
        private val REFERRAL_COUNT = intPreferencesKey("referral_count")
        private val SELECTED_PLAYER_IDS = stringSetPreferencesKey("selected_player_ids")
    }
    
    // Dark mode preference
    val darkMode: Flow<Boolean> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[DARK_MODE] ?: false
        }
    
    // Sound enabled preference
    val soundEnabled: Flow<Boolean> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[SOUND_ENABLED] ?: true
        }
    
    // Haptic feedback enabled preference
    val hapticFeedbackEnabled: Flow<Boolean> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] ?: true
        }
    
    // Background music enabled preference
    val backgroundMusicEnabled: Flow<Boolean> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[BACKGROUND_MUSIC_ENABLED] ?: false
        }
    
    // Content filter level preference
    val contentFilterLevel: Flow<ContentFilterLevel> = context.enhancedDataStore.data
        .map { preferences ->
            val levelString = preferences[CONTENT_FILTER_LEVEL] ?: ContentFilterLevel.MEDIUM.name
            try {
                ContentFilterLevel.valueOf(levelString)
            } catch (e: Exception) {
                ContentFilterLevel.MEDIUM
            }
        }
    
    // Premium subscription preference
    val premiumSubscription: Flow<Boolean> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[PREMIUM_SUBSCRIPTION] ?: false
        }
    
    // Last daily dare date preference
    val lastDailyDareDate: Flow<Long> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[LAST_DAILY_DARE_DATE] ?: 0L
        }
    
    // Notification enabled preference
    val notificationEnabled: Flow<Boolean> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_ENABLED] ?: true
        }
    
    // Last app version preference
    val lastAppVersion: Flow<Int> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[LAST_APP_VERSION] ?: 0
        }
    
    // Total play time preference
    val totalPlayTime: Flow<Long> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[TOTAL_PLAY_TIME] ?: 0L
        }
    
    // Total games played preference
    val totalGamesPlayed: Flow<Int> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[TOTAL_GAMES_PLAYED] ?: 0
        }
    
    // Referral code preference
    val referralCode: Flow<String?> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[REFERRAL_CODE]
        }
    
    // Referral count preference
    val referralCount: Flow<Int> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[REFERRAL_COUNT] ?: 0
        }
    
    // Selected player IDs preference
    val selectedPlayerIds: Flow<List<String>> = context.enhancedDataStore.data
        .map { preferences ->
            preferences[SELECTED_PLAYER_IDS]?.toList() ?: emptyList()
        }
    
    /**
     * Set dark mode
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.enhancedDataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }
    
    /**
     * Set sound enabled
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.enhancedDataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }
    
    /**
     * Set haptic feedback enabled
     */
    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.enhancedDataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }
    
    /**
     * Set background music enabled
     */
    suspend fun setBackgroundMusicEnabled(enabled: Boolean) {
        context.enhancedDataStore.edit { preferences ->
            preferences[BACKGROUND_MUSIC_ENABLED] = enabled
        }
    }
    
    /**
     * Set content filter level
     */
    suspend fun setContentFilterLevel(level: ContentFilterLevel) {
        context.enhancedDataStore.edit { preferences ->
            preferences[CONTENT_FILTER_LEVEL] = level.name
        }
    }
    
    /**
     * Set premium subscription
     */
    suspend fun setPremiumSubscription(subscribed: Boolean) {
        context.enhancedDataStore.edit { preferences ->
            preferences[PREMIUM_SUBSCRIPTION] = subscribed
        }
    }
    
    /**
     * Set last daily dare date
     */
    suspend fun setLastDailyDareDate(date: Long) {
        context.enhancedDataStore.edit { preferences ->
            preferences[LAST_DAILY_DARE_DATE] = date
        }
    }
    
    /**
     * Set notification enabled
     */
    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.enhancedDataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }
    
    /**
     * Set last app version
     */
    suspend fun setLastAppVersion(version: Int) {
        context.enhancedDataStore.edit { preferences ->
            preferences[LAST_APP_VERSION] = version
        }
    }
    
    /**
     * Increment total play time
     */
    suspend fun incrementTotalPlayTime(additionalTime: Long) {
        context.enhancedDataStore.edit { preferences ->
            val currentTime = preferences[TOTAL_PLAY_TIME] ?: 0L
            preferences[TOTAL_PLAY_TIME] = currentTime + additionalTime
        }
    }
    
    /**
     * Increment total games played
     */
    suspend fun incrementTotalGamesPlayed() {
        context.enhancedDataStore.edit { preferences ->
            val currentCount = preferences[TOTAL_GAMES_PLAYED] ?: 0
            preferences[TOTAL_GAMES_PLAYED] = currentCount + 1
        }
    }
    
    /**
     * Set referral code
     */
    suspend fun setReferralCode(code: String) {
        context.enhancedDataStore.edit { preferences ->
            preferences[REFERRAL_CODE] = code
        }
    }
    
    /**
     * Increment referral count
     */
    suspend fun incrementReferralCount() {
        context.enhancedDataStore.edit { preferences ->
            val currentCount = preferences[REFERRAL_COUNT] ?: 0
            preferences[REFERRAL_COUNT] = currentCount + 1
        }
    }
    
    /**
     * Set selected player IDs
     */
    suspend fun setSelectedPlayerIds(playerIds: List<String>) {
        context.enhancedDataStore.edit { preferences ->
            preferences[SELECTED_PLAYER_IDS] = playerIds.toSet()
        }
    }
}