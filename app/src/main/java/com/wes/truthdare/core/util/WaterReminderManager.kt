package com.wes.truthdare.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for water reminder notifications during drinking games
 */
@Singleton
class WaterReminderManager @Inject constructor() {
    
    // State flow for the reminder state
    private val _reminderState = MutableStateFlow<ReminderState>(ReminderState.Idle)
    val reminderState: StateFlow<ReminderState> = _reminderState
    
    // Job for the reminder timer
    private var reminderJob: Job? = null
    
    /**
     * Start water reminders with the specified interval
     * @param intervalMinutes The interval in minutes between reminders
     */
    fun startReminders(intervalMinutes: Int = 20) {
        // Cancel any existing reminder job
        reminderJob?.cancel()
        
        // Start a new reminder job
        reminderJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                // Wait for the specified interval
                delay(intervalMinutes * 60 * 1000L)
                
                // Show the reminder
                _reminderState.value = ReminderState.ShowReminder
            }
        }
    }
    
    /**
     * Stop water reminders
     */
    fun stopReminders() {
        reminderJob?.cancel()
        reminderJob = null
        _reminderState.value = ReminderState.Idle
    }
    
    /**
     * Dismiss the current water reminder and start a new timer
     */
    fun dismissAndRemindLater() {
        _reminderState.value = ReminderState.Idle
        
        // Restart the timer
        reminderJob?.cancel()
        startReminders()
    }
    
    /**
     * Dismiss the current water reminder without starting a new timer
     */
    fun dismissReminder() {
        _reminderState.value = ReminderState.Idle
    }
    
    /**
     * Reminder state
     */
    sealed class ReminderState {
        /**
         * No active reminder
         */
        object Idle : ReminderState()
        
        /**
         * Show a water reminder
         */
        object ShowReminder : ReminderState()
    }
}