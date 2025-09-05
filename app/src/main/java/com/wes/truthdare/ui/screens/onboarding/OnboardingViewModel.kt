package com.wes.truthdare.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.asr.AssetUnpacker
import com.wes.truthdare.core.data.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the onboarding screen
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val assetUnpacker: AssetUnpacker
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    // Check if model needs to be unpacked
    init {
        viewModelScope.launch {
            val modelUnpacked = assetUnpacker.isModelUnpacked()
            _uiState.value = _uiState.value.copy(
                modelUnpacked = modelUnpacked,
                currentStep = if (modelUnpacked) 0 else -1
            )
        }
    }
    
    /**
     * Start unpacking the ASR model
     */
    fun startUnpackingModel() {
        if (_uiState.value.modelUnpacked) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(unpackingInProgress = true)
            
            assetUnpacker.unpackModel().collect { progress ->
                _uiState.value = _uiState.value.copy(unpackingProgress = progress)
            }
            
            _uiState.value = _uiState.value.copy(
                modelUnpacked = true,
                unpackingInProgress = false,
                currentStep = 0
            )
        }
    }
    
    /**
     * Navigate to the next onboarding step
     */
    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep < TOTAL_STEPS - 1) {
            _uiState.value = _uiState.value.copy(currentStep = currentStep + 1)
        }
    }
    
    /**
     * Navigate to the previous onboarding step
     */
    fun previousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep > 0) {
            _uiState.value = _uiState.value.copy(currentStep = currentStep - 1)
        }
    }
    
    /**
     * Complete the onboarding process
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            appPreferences.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(onboardingCompleted = true)
        }
    }
    
    companion object {
        private const val TOTAL_STEPS = 3
    }
}

/**
 * UI state for the onboarding screen
 */
data class OnboardingUiState(
    val currentStep: Int = -1, // -1 means model unpacking needed
    val modelUnpacked: Boolean = false,
    val unpackingInProgress: Boolean = false,
    val unpackingProgress: Float = 0f,
    val onboardingCompleted: Boolean = false
)