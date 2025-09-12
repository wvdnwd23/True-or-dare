package com.wes.truthdare.ui.screens.gamemode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.repositories.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Game Mode Selection screen
 */
@HiltViewModel
class GameModeSelectionViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    // UI State for Game Mode Selection
    private val _uiState = MutableStateFlow(GameModeSelectionUiState())
    val uiState: StateFlow<GameModeSelectionUiState> = _uiState.asStateFlow()

    init {
        // Load selected players from preferences
        viewModelScope.launch {
            appPreferences.selectedPlayerIds.collect { playerIds ->
                _uiState.update { currentState ->
                    currentState.copy(selectedPlayerIds = playerIds)
                }
            }
        }
        
        // Load all players
        viewModelScope.launch {
            playerRepository.getAllPlayers().collect { players ->
                _uiState.update { currentState ->
                    currentState.copy(allPlayers = players)
                }
            }
        }
    }

    /**
     * Set the selected player IDs
     */
    fun setSelectedPlayerIds(playerIds: List<String>) {
        viewModelScope.launch {
            appPreferences.setSelectedPlayerIds(playerIds)
        }
    }
}

/**
 * UI State for Game Mode Selection
 */
data class GameModeSelectionUiState(
    val selectedPlayerIds: List<String> = emptyList(),
    val allPlayers: List<com.wes.truthdare.core.data.entities.PlayerEntity> = emptyList()
)