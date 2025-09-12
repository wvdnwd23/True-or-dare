package com.wes.truthdare.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.data.repositories.ReportEntity
import com.wes.truthdare.core.data.repositories.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the report list screen
 */
@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {
    
    // UI state
    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: StateFlow<ReportListUiState> = _uiState.asStateFlow()
    
    // ID of the report to delete
    private var reportToDeleteId: String? = null
    
    init {
        // Load reports
        viewModelScope.launch {
            reportRepository.getAllReports().collect { reports ->
                _uiState.value = _uiState.value.copy(reports = reports)
            }
        }
    }
    
    /**
     * Show the delete confirmation dialog
     * @param reportId The ID of the report to delete
     */
    fun showDeleteConfirmation(reportId: String) {
        reportToDeleteId = reportId
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
    }
    
    /**
     * Dismiss the delete confirmation dialog
     */
    fun dismissDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
    }
    
    /**
     * Confirm deletion of the report
     */
    fun confirmDelete() {
        val id = reportToDeleteId ?: return
        
        viewModelScope.launch {
            reportRepository.deleteReport(id)
            reportToDeleteId = null
        }
    }
    
    /**
     * Show the delete all confirmation dialog
     */
    fun showDeleteAllConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteAllConfirmation = true)
    }
    
    /**
     * Dismiss the delete all confirmation dialog
     */
    fun dismissDeleteAllConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteAllConfirmation = false)
    }
    
    /**
     * Confirm deletion of all reports
     */
    fun confirmDeleteAll() {
        viewModelScope.launch {
            reportRepository.deleteAllReports()
        }
    }
}

/**
 * UI state for the report list screen
 */
data class ReportListUiState(
    val reports: List<ReportEntity> = emptyList(),
    val showDeleteConfirmation: Boolean = false,
    val showDeleteAllConfirmation: Boolean = false
)