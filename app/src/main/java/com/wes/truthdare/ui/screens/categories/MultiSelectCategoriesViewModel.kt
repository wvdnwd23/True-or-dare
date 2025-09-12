package com.wes.truthdare.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.IntensityLevel
import com.wes.truthdare.core.data.models.Category
import com.wes.truthdare.core.data.models.EnhancedQuestionV2
import com.wes.truthdare.core.data.models.SubOption
import com.wes.truthdare.core.data.repositories.CategoryRepository
import com.wes.truthdare.core.data.repositories.GameSessionRepository
import com.wes.truthdare.core.data.repositories.PlayerRepository
import com.wes.truthdare.core.data.repositories.QuestionRepository
import com.wes.truthdare.core.data.repositories.SubOptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the multi-select categories screen
 */
@HiltViewModel
class MultiSelectCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val subOptionRepository: SubOptionRepository,
    private val questionRepository: QuestionRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val playerRepository: PlayerRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(CategorySelectionState())
    val uiState: StateFlow<CategorySelectionState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Load categories and sub-options
            val categories = categoryRepository.getAllCategories()
            val subOptions = subOptionRepository.getAllSubOptions()
            
            // Load default values from preferences
            val defaultIntensityLevel = appPreferences.defaultHeatLevel.first()
            
            _uiState.update { currentState ->
                currentState.copy(
                    categories = categories,
                    subOptions = subOptions,
                    intensitySliderValue = defaultIntensityLevel,
                    maxIntensityLevel = IntensityLevel.fromSliderValue(defaultIntensityLevel)
                )
            }
        }
    }
    
    /**
     * Set player IDs for the game
     * @param playerIds The player IDs
     */
    fun setPlayerIds(playerIds: List<String>) {
        viewModelScope.launch {
            val players = playerRepository.getPlayersByIds(playerIds)
            _uiState.update { currentState ->
                currentState.copy(
                    playerNames = players.map { it.name }
                )
            }
        }
    }
    
    /**
     * Toggle a category selection
     * @param categoryId The category ID to toggle
     */
    fun toggleCategory(categoryId: String) {
        val currentSelectedCategories = _uiState.value.selectedCategoryIds.toMutableSet()
        val currentSelectedSubOptions = _uiState.value.selectedSubOptionIds.toMutableMap()
        
        if (currentSelectedCategories.contains(categoryId)) {
            // Deselect category
            currentSelectedCategories.remove(categoryId)
            currentSelectedSubOptions.remove(categoryId)
        } else {
            // Select category
            currentSelectedCategories.add(categoryId)
            
            // Auto-select all sub-options for this category
            val subOptionsForCategory = _uiState.value.getSubOptionsForCategory(categoryId)
            if (subOptionsForCategory.isNotEmpty()) {
                currentSelectedSubOptions[categoryId] = subOptionsForCategory.map { it.id }.toSet()
            }
        }
        
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategoryIds = currentSelectedCategories,
                selectedSubOptionIds = currentSelectedSubOptions
            )
        }
    }
    
    /**
     * Toggle a sub-option selection
     * @param categoryId The category ID
     * @param subOptionId The sub-option ID to toggle
     */
    fun toggleSubOption(categoryId: String, subOptionId: String) {
        val currentSelectedSubOptions = _uiState.value.selectedSubOptionIds.toMutableMap()
        val subOptionsForCategory = currentSelectedSubOptions[categoryId]?.toMutableSet() ?: mutableSetOf()
        
        if (subOptionsForCategory.contains(subOptionId)) {
            // Deselect sub-option
            subOptionsForCategory.remove(subOptionId)
        } else {
            // Select sub-option
            subOptionsForCategory.add(subOptionId)
        }
        
        currentSelectedSubOptions[categoryId] = subOptionsForCategory
        
        _uiState.update { currentState ->
            currentState.copy(
                selectedSubOptionIds = currentSelectedSubOptions
            )
        }
    }
    
    /**
     * Set the intensity slider value
     * @param value The intensity value (0-100)
     */
    fun setIntensitySliderValue(value: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                intensitySliderValue = value.coerceIn(0, 100),
                maxIntensityLevel = IntensityLevel.fromSliderValue(value.coerceIn(0, 100))
            )
        }
    }
    
    /**
     * Set age verified status
     * @param verified True if age is verified
     */
    fun setAgeVerified(verified: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                ageVerified = verified
            )
        }
    }
    
    /**
     * Set trauma healing opted in status
     * @param optedIn True if opted in
     */
    fun setTraumaHealingOptedIn(optedIn: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                traumaHealingOptedIn = optedIn
            )
        }
    }
    
    /**
     * Clear all selections
     */
    fun clearAllSelections() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategoryIds = emptySet(),
                selectedSubOptionIds = emptyMap()
            )
        }
    }
    
    /**
     * Apply a quick pick preset
     * @param preset The quick pick preset to apply
     */
    fun applyQuickPick(preset: QuickPickPreset) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategoryIds = preset.categoryIds,
                selectedSubOptionIds = preset.subOptionIds,
                intensitySliderValue = preset.intensityValue,
                maxIntensityLevel = IntensityLevel.fromSliderValue(preset.intensityValue)
            )
        }
    }
    
    /**
     * Generate preview questions for selected categories
     */
    fun generatePreviewQuestions() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedCategoryIds = state.selectedCategoryIds
            val selectedSubOptionIds = state.getAllSelectedSubOptionIds()
            val maxIntensityLevel = state.maxIntensityLevel
            
            // Get sample questions for preview
            val previewQuestions = questionRepository.getSampleQuestionsForPreview(
                categoryIds = selectedCategoryIds.toList(),
                subOptionIds = selectedSubOptionIds.toList(),
                maxIntensityLevel = maxIntensityLevel,
                count = 5
            )
            
            _uiState.update { currentState ->
                currentState.copy(
                    previewQuestions = previewQuestions
                )
            }
        }
    }
    
    /**
     * Create a new game session
     * @return The ID of the created session
     */
    suspend fun createSession(): String {
        val state = _uiState.value
        
        // Ensure we have at least one category
        if (state.selectedCategoryIds.isEmpty()) {
            throw IllegalStateException("At least one category must be selected")
        }
        
        // Create the session
        val session = gameSessionRepository.createMultiSelectSession(
            playerIds = state.playerNames.indices.map { it.toString() }, // Placeholder, replace with actual player IDs
            categoryIds = state.selectedCategoryIds.toList(),
            subOptionIds = state.getAllSelectedSubOptionIds().toList(),
            maxIntensityLevel = state.maxIntensityLevel,
            ageVerified = state.ageVerified,
            traumaHealingOptedIn = state.traumaHealingOptedIn
        )
        
        // Save preferences
        appPreferences.setDefaultHeatLevel(state.intensitySliderValue)
        appPreferences.setCurrentSessionId(session.id)
        
        return session.id
    }
}