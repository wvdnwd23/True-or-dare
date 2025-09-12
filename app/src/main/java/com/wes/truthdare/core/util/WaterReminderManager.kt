package com.wes.truthdare.core.util

import android.content.Context
import android.os.CountDownTimer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for water reminder notifications during Drink Mode
 */
@Singleton
class WaterReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Default reminder interval in milliseconds (20 minutes)
    private val DEFAULT_REMINDER_INTERVAL = 20 * 60 * 1000L
    
    // Countdown timer for water reminders
    private var reminderTimer: CountDownTimer? = null
    
    // State flow for reminder status
    private val _reminderState = MutableStateFlow<ReminderState>(ReminderState.Inactive)
    val reminderState: StateFlow<ReminderState> = _reminderState.asStateFlow()
    
    /**
     * Start the water reminder timer
     * @param intervalMinutes The interval in minutes between reminders
     */
    fun startReminders(intervalMinutes: Int = 20) {
        // Cancel any existing timer
        stopReminders()
        
        val intervalMillis = intervalMinutes * 60 * 1000L
        
        // Update state to active with the remaining time
        _reminderState.value = ReminderState.Active(intervalMillis)
        
        // Create and start a new timer
        reminderTimer = object : CountDownTimer(intervalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _reminderState.value = ReminderState.Active(millisUntilFinished)
            }
            
            override fun onFinish() {
                _reminderState.value = ReminderState.ShowReminder
            }
        }.start()
    }
    
    /**
     * Stop the water reminder timer
     */
    fun stopReminders() {
        reminderTimer?.cancel()
        reminderTimer = null
        _reminderState.value = ReminderState.Inactive
    }
    
    /**
     * Dismiss the current reminder and start a new timer
     */
    fun dismissAndRemindLater() {
        startReminders()
    }
    
    /**
     * Dismiss the current reminder without starting a new timer
     */
    fun dismissReminder() {
        _reminderState.value = ReminderState.Inactive
    }
    
    /**
     * States for the water reminder
     */
    sealed class ReminderState {
        /**
         * No active reminder
         */
        object Inactive : ReminderState()
        
        /**
         * Reminder is active and counting down
         * @param remainingTimeMillis The remaining time in milliseconds
         */
        data class Active(val remainingTimeMillis: Long) : ReminderState()
        
        /**
         * Time to show the reminder
         */
        object ShowReminder : ReminderState()
    }
}