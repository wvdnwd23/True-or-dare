package com.wes.truthdare.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.asr.AssetUnpacker
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.repositories.GameSessionRepository
import com.wes.truthdare.core.data.repositories.JournalRepository
import com.wes.truthdare.core.data.repositories.PlayerPreferenceRepository
import com.wes.truthdare.core.data.repositories.PlayerRepository
import com.wes.truthdare.core.data.repositories.QuestionHistoryRepository
import com.wes.truthdare.core.data.repositories.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val playerRepository: PlayerRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val questionHistoryRepository: QuestionHistoryRepository,
    private val playerPreferenceRepository: PlayerPreferenceRepository,
    private val journalRepository: JournalRepository,
    private val questionRepository: QuestionRepository,
    private val assetUnpacker: AssetUnpacker
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Load settings from preferences
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // Load voice mode setting
            appPreferences.voiceModeEnabled.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(voiceModeEnabled = enabled)
            }
        }
        
        viewModelScope.launch {
            // Load journal setting
            appPreferences.journalEnabled.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(journalEnabled = enabled)
            }
        }
        
        viewModelScope.launch {
            // Load haptics setting
            appPreferences.hapticsEnabled.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(hapticsEnabled = enabled)
            }
        }
        
        viewModelScope.launch {
            // Load volume setting
            appPreferences.volumeLevel.collectLatest { level ->
                _uiState.value = _uiState.value.copy(volumeLevel = level)
            }
        }
        
        viewModelScope.launch {
            // Load depth level setting
            appPreferences.defaultDepthLevel.collectLatest { level ->
                _uiState.value = _uiState.value.copy(defaultDepthLevel = level)
            }
        }
    }
    
    /**
     * Set voice mode enabled
     * @param enabled Whether voice mode is enabled
     */
    fun setVoiceModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setVoiceModeEnabled(enabled)
            _uiState.value = _uiState.value.copy(voiceModeEnabled = enabled)
        }
    }
    
    /**
     * Set journal enabled
     * @param enabled Whether journal is enabled
     */
    fun setJournalEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setJournalEnabled(enabled)
            _uiState.value = _uiState.value.copy(journalEnabled = enabled)
        }
    }
    
    /**
     * Set haptics enabled
     * @param enabled Whether haptics are enabled
     */
    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setHapticsEnabled(enabled)
            _uiState.value = _uiState.value.copy(hapticsEnabled = enabled)
        }
    }
    
    /**
     * Set volume level
     * @param level The volume level (0-100)
     */
    fun setVolumeLevel(level: Int) {
        viewModelScope.launch {
            appPreferences.setVolumeLevel(level)
            _uiState.value = _uiState.value.copy(volumeLevel = level)
        }
    }
    
    /**
     * Set default depth level
     * @param level The default depth level (1-5)
     */
    fun setDefaultDepthLevel(level: Int) {
        viewModelScope.launch {
            appPreferences.setDefaultDepthLevel(level)
            _uiState.value = _uiState.value.copy(defaultDepthLevel = level)
        }
    }
    
    /**
     * Wipe all data
     */
    fun wipeAllData() {
        viewModelScope.launch {
            // Clear preferences
            appPreferences.clearAllPreferences()
            
            // Clear database
            playerRepository.deleteAllPlayers()
            gameSessionRepository.deleteAllSessions()
            questionHistoryRepository.deleteAllQuestionHistory()
            playerPreferenceRepository.deleteAllPreferences()
            journalRepository.deleteAllEntries()
            
            // Clear question cache
            questionRepository.clearCache()
            
            // Clean up model files
            assetUnpacker.cleanupModel()
            
            // Reset UI state
            _uiState.value = SettingsUiState(
                voiceModeEnabled = false,
                journalEnabled = false,
                hapticsEnabled = true,
                volumeLevel = 50,
                defaultDepthLevel = 1,
                isWipingData = false
            )
        }
    }
    
    /**
     * Set wiping data state
     * @param isWiping Whether data is being wiped
     */
    fun setWipingData(isWiping: Boolean) {
        _uiState.value = _uiState.value.copy(isWipingData = isWiping)
    }
}

/**
 * UI state for the settings screen
 */
data class SettingsUiState(
    val voiceModeEnabled: Boolean = false,
    val journalEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val volumeLevel: Int = 50,
    val defaultDepthLevel: Int = 1,
    val isWipingData: Boolean = false
)