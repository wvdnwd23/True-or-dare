package com.wes.truthdare.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.repositories.GameSessionRepository
import com.wes.truthdare.core.data.repositories.PlayerRepository
import com.wes.truthdare.core.selector.GameMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the categories screen
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val gameSessionRepository: GameSessionRepository,
    private val playerRepository: PlayerRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Load default values from preferences
            val defaultGameMode = appPreferences.defaultGameMode.first()
            val defaultHeatLevel = appPreferences.defaultHeatLevel.first()
            val defaultDepthLevel = appPreferences.defaultDepthLevel.first()
            
            _uiState.value = _uiState.value.copy(
                gameMode = GameMode.valueOf(defaultGameMode),
                heatLevel = defaultHeatLevel,
                depthLevel = defaultDepthLevel
            )
        }
    }
    
    /**
     * Set player IDs for the game
     * @param playerIds The player IDs
     */
    fun setPlayerIds(playerIds: List<String>) {
        viewModelScope.launch {
            val players = playerRepository.getPlayersByIds(playerIds)
            _uiState.value = _uiState.value.copy(
                playerIds = playerIds,
                playerNames = players.map { it.name }
            )
        }
    }
    
    /**
     * Toggle a category selection
     * @param category The category to toggle
     */
    fun toggleCategory(category: String) {
        val currentCategories = _uiState.value.selectedCategories.toMutableSet()
        if (currentCategories.contains(category)) {
            currentCategories.remove(category)
        } else {
            currentCategories.add(category)
        }
        _uiState.value = _uiState.value.copy(selectedCategories = currentCategories)
    }
    
    /**
     * Set the game mode
     * @param mode The game mode
     */
    fun setGameMode(mode: GameMode) {
        _uiState.value = _uiState.value.copy(gameMode = mode)
        
        // Update default categories based on mode
        updateDefaultCategories(mode)
    }
    
    /**
     * Set the heat level
     * @param level The heat level (0-100)
     */
    fun setHeatLevel(level: Int) {
        _uiState.value = _uiState.value.copy(heatLevel = level.coerceIn(0, 100))
    }
    
    /**
     * Set the depth level
     * @param level The depth level (1-5)
     */
    fun setDepthLevel(level: Int) {
        _uiState.value = _uiState.value.copy(depthLevel = level.coerceIn(1, 5))
    }
    
    /**
     * Create a new game session
     * @return The ID of the created session
     */
    suspend fun createSession(): String {
        val state = _uiState.value
        
        // Ensure we have at least one category
        val categories = if (state.selectedCategories.isEmpty()) {
            getDefaultCategories(state.gameMode)
        } else {
            state.selectedCategories.toList()
        }
        
        // Create the session
        val session = gameSessionRepository.createSession(
            playerIds = state.playerIds,
            mode = state.gameMode,
            categories = categories,
            maxHeat = state.heatLevel,
            maxDepth = state.depthLevel
        )
        
        // Save preferences
        appPreferences.setDefaultGameMode(state.gameMode.name)
        appPreferences.setDefaultHeatLevel(state.heatLevel)
        appPreferences.setDefaultDepthLevel(state.depthLevel)
        appPreferences.setCurrentSessionId(session.id)
        
        // Update last played timestamp for players
        state.playerIds.forEach { playerId ->
            playerRepository.updatePlayerLastPlayed(playerId)
        }
        
        return session.id
    }
    
    /**
     * Update default categories based on game mode
     * @param mode The game mode
     */
    private fun updateDefaultCategories(mode: GameMode) {
        val defaultCategories = getDefaultCategories(mode).toSet()
        
        // Keep existing selections that are valid for this mode
        val currentCategories = _uiState.value.selectedCategories
        val updatedCategories = currentCategories.filter { 
            defaultCategories.contains(it) 
        }.toMutableSet()
        
        // If no valid categories remain, use defaults
        if (updatedCategories.isEmpty()) {
            updatedCategories.addAll(defaultCategories)
        }
        
        _uiState.value = _uiState.value.copy(selectedCategories = updatedCategories)
    }
    
    /**
     * Get default categories for a game mode
     * @param mode The game mode
     * @return List of default categories
     */
    private fun getDefaultCategories(mode: GameMode): List<String> {
        return when (mode) {
            GameMode.CASUAL -> listOf("casual", "friends", "funny", "hypothetical")
            GameMode.PARTY -> listOf("party", "funny", "challenge", "hypothetical")
            GameMode.DEEP_TALK -> listOf("deep", "personal", "future", "hypothetical")
            GameMode.ROMANTIC -> listOf("romantic", "personal", "deep", "future")
            GameMode.FAMILY_FRIENDLY -> listOf("family", "casual", "childhood", "funny")
        }
    }
}

/**
 * UI state for the categories screen
 */
data class CategoriesUiState(
    val playerIds: List<String> = emptyList(),
    val playerNames: List<String> = emptyList(),
    val gameMode: GameMode = GameMode.CASUAL,
    val selectedCategories: Set<String> = setOf("casual", "friends", "funny", "hypothetical"),
    val heatLevel: Int = 50,
    val depthLevel: Int = 1
)