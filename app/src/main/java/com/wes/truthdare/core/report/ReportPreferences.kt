package com.wes.truthdare.core.report

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences for report generation
 */
data class ReportPrefs(
    val storeAnswers: Boolean,
    val redactAnswers: Boolean,
    val anonymize: Boolean,
    val theme: Theme,
    val watermark: Boolean,
    val isPremium: Boolean
)

/**
 * Storage options for reports
 */
enum class ReportStorageOption {
    SAVE_LOCALLY,
    GENERATE_ONCE,
    NEVER_SAVE_ANSWERS
}

/**
 * Manager for report preferences
 */
@Singleton
class ReportPreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val REPORT_STORAGE_OPTION = stringPreferencesKey("report_storage_option")
        private val REDACT_ANSWERS = booleanPreferencesKey("redact_answers")
        private val ANONYMIZE = booleanPreferencesKey("anonymize")
        private val REPORT_THEME = stringPreferencesKey("report_theme")
        private val WATERMARK = booleanPreferencesKey("watermark")
        private val CONSENT_GIVEN = booleanPreferencesKey("report_consent_given")
    }
    
    /**
     * Get the current report preferences
     */
    val reportPrefs: Flow<ReportPrefs> = dataStore.data.map { preferences ->
        val storageOption = preferences[REPORT_STORAGE_OPTION]?.let {
            ReportStorageOption.valueOf(it)
        } ?: ReportStorageOption.GENERATE_ONCE
        
        val redactAnswers = preferences[REDACT_ANSWERS] ?: false
        val anonymize = preferences[ANONYMIZE] ?: false
        val theme = preferences[REPORT_THEME]?.let {
            Theme.valueOf(it)
        } ?: Theme.LIGHT
        val watermark = preferences[WATERMARK] ?: true
        
        // For now, hardcode isPremium to false
        // In a real app, this would come from a purchase verification system
        val isPremium = false
        
        ReportPrefs(
            storeAnswers = storageOption == ReportStorageOption.SAVE_LOCALLY,
            redactAnswers = redactAnswers,
            anonymize = anonymize,
            theme = theme,
            watermark = watermark,
            isPremium = isPremium
        )
    }
    
    /**
     * Check if consent has been given for report generation
     */
    val consentGiven: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CONSENT_GIVEN] ?: false
    }
    
    /**
     * Set the report storage option
     */
    suspend fun setReportStorageOption(option: ReportStorageOption) {
        dataStore.edit { preferences ->
            preferences[REPORT_STORAGE_OPTION] = option.name
        }
    }
    
    /**
     * Set whether to redact answers in reports
     */
    suspend fun setRedactAnswers(redact: Boolean) {
        dataStore.edit { preferences ->
            preferences[REDACT_ANSWERS] = redact
        }
    }
    
    /**
     * Set whether to anonymize player names in reports
     */
    suspend fun setAnonymize(anonymize: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANONYMIZE] = anonymize
        }
    }
    
    /**
     * Set the report theme
     */
    suspend fun setReportTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[REPORT_THEME] = theme.name
        }
    }
    
    /**
     * Set whether to include a watermark in reports
     */
    suspend fun setWatermark(watermark: Boolean) {
        dataStore.edit { preferences ->
            preferences[WATERMARK] = watermark
        }
    }
    
    /**
     * Set whether consent has been given for report generation
     */
    suspend fun setConsentGiven(consent: Boolean) {
        dataStore.edit { preferences ->
            preferences[CONSENT_GIVEN] = consent
        }
    }
}