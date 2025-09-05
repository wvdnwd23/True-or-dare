package com.wes.truthdare.ui.screens.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.data.entities.PlayerEntity
import com.wes.truthdare.core.data.repositories.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the players screen
 */
@HiltViewModel
class PlayersViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(PlayersUiState())
    
    // Players from repository
    private val players = playerRepository.getAllPlayers()
    
    // Combined state
    val state: StateFlow<PlayersUiState> = combine(
        _uiState,
        players
    ) { uiState, playersList ->
        uiState.copy(
            players = playersList,
            selectedPlayerIds = uiState.selectedPlayerIds.filter { playerId ->
                playersList.any { it.id == playerId }
            }.toSet()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayersUiState()
    )
    
    /**
     * Create a new player
     * @param name The player name
     */
    fun createPlayer(name: String) {
        if (name.isBlank()) return
        
        viewModelScope.launch {
            // Generate a random color for the player avatar
            val avatarColor = generateRandomColor()
            playerRepository.createPlayer(name, avatarColor)
            _uiState.value = _uiState.value.copy(newPlayerName = "")
        }
    }
    
    /**
     * Delete a player
     * @param player The player to delete
     */
    fun deletePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            playerRepository.deletePlayer(player)
            
            // Remove from selected players if selected
            val updatedSelectedPlayerIds = _uiState.value.selectedPlayerIds.toMutableSet()
            updatedSelectedPlayerIds.remove(player.id)
            _uiState.value = _uiState.value.copy(selectedPlayerIds = updatedSelectedPlayerIds)
        }
    }
    
    /**
     * Toggle player selection
     * @param playerId The player ID to toggle
     */
    fun togglePlayerSelection(playerId: String) {
        val selectedPlayerIds = _uiState.value.selectedPlayerIds.toMutableSet()
        if (selectedPlayerIds.contains(playerId)) {
            selectedPlayerIds.remove(playerId)
        } else {
            selectedPlayerIds.add(playerId)
        }
        _uiState.value = _uiState.value.copy(selectedPlayerIds = selectedPlayerIds)
    }
    
    /**
     * Update the new player name
     * @param name The new player name
     */
    fun updateNewPlayerName(name: String) {
        _uiState.value = _uiState.value.copy(newPlayerName = name)
    }
    
    /**
     * Generate a random color for player avatars
     * @return A random color as an ARGB integer
     */
    private fun generateRandomColor(): Int {
        val colors = listOf(
            0xFFE57373.toInt(), // Red
            0xFFF06292.toInt(), // Pink
            0xFFBA68C8.toInt(), // Purple
            0xFF9575CD.toInt(), // Deep Purple
            0xFF7986CB.toInt(), // Indigo
            0xFF64B5F6.toInt(), // Blue
            0xFF4FC3F7.toInt(), // Light Blue
            0xFF4DD0E1.toInt(), // Cyan
            0xFF4DB6AC.toInt(), // Teal
            0xFF81C784.toInt(), // Green
            0xFFAED581.toInt(), // Light Green
            0xFFFFD54F.toInt(), // Yellow
            0xFFFFB74D.toInt(), // Orange
            0xFFA1887F.toInt()  // Brown
        )
        return colors[Random.nextInt(colors.size)]
    }
}

/**
 * UI state for the players screen
 */
data class PlayersUiState(
    val players: List<PlayerEntity> = emptyList(),
    val selectedPlayerIds: Set<String> = emptySet(),
    val newPlayerName: String = ""
)