package com.wes.truthdare.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.data.SessionLogger
import com.wes.truthdare.core.data.repositories.ReportRepository
import com.wes.truthdare.core.report.DefaultReportBuilder
import com.wes.truthdare.core.report.Report
import com.wes.truthdare.core.report.ReportBuilder
import com.wes.truthdare.core.report.ReportExporter
import com.wes.truthdare.core.report.ReportPreferencesManager
import com.wes.truthdare.core.report.ReportStorageOption
import com.wes.truthdare.core.report.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the report screen
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sessionLogger: SessionLogger,
    private val reportBuilder: ReportBuilder,
    private val reportExporter: ReportExporter,
    private val reportRepository: ReportRepository,
    private val reportPreferencesManager: ReportPreferencesManager
) : ViewModel() {
    
    // UI state
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Check if consent has been given
            val consentGiven = reportPreferencesManager.consentGiven.first()
            
            if (!consentGiven) {
                _uiState.value = _uiState.value.copy(showConsentDialog = true)
            } else {
                // Load report preferences
                val prefs = reportPreferencesManager.reportPrefs.first()
                
                _uiState.value = _uiState.value.copy(
                    redactAnswers = prefs.redactAnswers,
                    anonymize = prefs.anonymize,
                    theme = prefs.theme,
                    watermark = prefs.watermark,
                    isPremium = prefs.isPremium
                )
                
                // Generate report from session log
                generateReport()
            }
        }
    }
    
    /**
     * Generate a report from the current session log
     */
    private fun generateReport() {
        val sessionLog = sessionLogger.getSessionLogSnapshot() ?: return
        
        // Build report
        val report = reportBuilder.build(
            log = sessionLog,
            redactAnswers = _uiState.value.redactAnswers,
            anonymize = _uiState.value.anonymize
        )
        
        // Update UI state
        _uiState.value = _uiState.value.copy(report = report)
        
        // Save report if needed
        viewModelScope.launch {
            val prefs = reportPreferencesManager.reportPrefs.first()
            
            if (prefs.storeAnswers) {
                // Save report to repository
                reportRepository.saveReport(report, sessionLog)
            }
        }
    }
    
    /**
     * Load a saved report by ID
     * @param reportId The ID of the report to load
     */
    fun loadReport(reportId: String) {
        viewModelScope.launch {
            val reportEntity = reportRepository.getReportById(reportId) ?: return@launch
            
            // Parse the JSON to create a Report object
            // In a real implementation, you would deserialize the JSON to a Report object
            // For simplicity, we'll just create a placeholder report
            
            // This is a simplified approach - in a real app, you would deserialize the JSON properly
            val jsonObject = JSONObject(reportEntity.reportJson)
            val summaryJson = jsonObject.getJSONObject("summary")
            
            // Create a placeholder report based on the saved data
            // In a real implementation, you would deserialize the full report
            val placeholderReport = Report(
                summary = com.wes.truthdare.core.report.ReportSummary(
                    sessionId = summaryJson.getString("sessionId"),
                    startTime = java.time.Instant.parse(summaryJson.getString("startTime")),
                    endTime = java.time.Instant.parse(summaryJson.getString("endTime")),
                    duration = java.time.Duration.parse(summaryJson.getString("duration")),
                    gameMode = reportEntity.gameMode,
                    drinkMode = summaryJson.getBoolean("drinkMode"),
                    playerCount = summaryJson.getInt("playerCount"),
                    roundCount = summaryJson.getInt("roundCount"),
                    truthCount = summaryJson.getInt("truthCount"),
                    dareCount = summaryJson.getInt("dareCount"),
                    skipCount = summaryJson.getInt("skipCount"),
                    totalShots = summaryJson.getInt("totalShots")
                ),
                perPlayer = emptyList(), // In a real implementation, you would deserialize the player data
                rounds = emptyList(),    // In a real implementation, you would deserialize the round data
                highlights = emptyList() // In a real implementation, you would deserialize the highlight data
            )
            
            // Update UI state
            _uiState.value = _uiState.value.copy(report = placeholderReport)
        }
    }
    
    /**
     * Export the report to PDF
     * @return The exported file, or null if export failed
     */
    suspend fun exportToPdf(): File? {
        val report = _uiState.value.report ?: return null
        
        return reportExporter.toPdf(
            report = report,
            theme = _uiState.value.theme,
            watermark = _uiState.value.watermark && !_uiState.value.isPremium
        )
    }
    
    /**
     * Export the report to CSV
     * @return The exported file, or null if export failed
     */
    suspend fun exportToCsv(): File? {
        val report = _uiState.value.report ?: return null
        
        return reportExporter.toCsv(report)
    }
    
    /**
     * Export the report to JSON
     * @return The exported file, or null if export failed
     */
    suspend fun exportToJson(): File? {
        val report = _uiState.value.report ?: return null
        
        return reportExporter.toJson(report)
    }
    
    /**
     * Show the export dialog
     */
    fun showExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = true)
    }
    
    /**
     * Dismiss the export dialog
     */
    fun dismissExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = false)
    }
    
    /**
     * Show the settings dialog
     */
    fun showSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = true)
    }
    
    /**
     * Dismiss the settings dialog
     */
    fun dismissSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = false)
    }
    
    /**
     * Dismiss the consent dialog
     */
    fun dismissConsentDialog() {
        _uiState.value = _uiState.value.copy(showConsentDialog = false)
    }
    
    /**
     * Set whether consent has been given for report generation
     */
    fun setConsentGiven(consent: Boolean) {
        viewModelScope.launch {
            reportPreferencesManager.setConsentGiven(consent)
            
            if (consent) {
                // Generate report
                generateReport()
            }
        }
    }
    
    /**
     * Set the report storage option
     */
    fun setReportStorageOption(option: ReportStorageOption) {
        viewModelScope.launch {
            reportPreferencesManager.setReportStorageOption(option)
            
            // Update UI state
            val prefs = reportPreferencesManager.reportPrefs.first()
            _uiState.value = _uiState.value.copy(
                redactAnswers = prefs.redactAnswers,
                anonymize = prefs.anonymize,
                theme = prefs.theme,
                watermark = prefs.watermark,
                isPremium = prefs.isPremium
            )
        }
    }
    
    /**
     * Update report settings
     */
    fun updateSettings(redact: Boolean, anonymize: Boolean, theme: Theme, watermark: Boolean) {
        viewModelScope.launch {
            reportPreferencesManager.setRedactAnswers(redact)
            reportPreferencesManager.setAnonymize(anonymize)
            reportPreferencesManager.setReportTheme(theme)
            
            // Only update watermark if premium
            if (_uiState.value.isPremium) {
                reportPreferencesManager.setWatermark(watermark)
            }
            
            // Update UI state
            _uiState.value = _uiState.value.copy(
                redactAnswers = redact,
                anonymize = anonymize,
                theme = theme,
                watermark = if (_uiState.value.isPremium) watermark else true
            )
            
            // Regenerate report with new settings
            generateReport()
        }
    }
}

/**
 * UI state for the report screen
 */
data class ReportUiState(
    val report: Report? = null,
    val showExportDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showConsentDialog: Boolean = false,
    val redactAnswers: Boolean = false,
    val anonymize: Boolean = false,
    val theme: Theme = Theme.LIGHT,
    val watermark: Boolean = true,
    val isPremium: Boolean = false
)