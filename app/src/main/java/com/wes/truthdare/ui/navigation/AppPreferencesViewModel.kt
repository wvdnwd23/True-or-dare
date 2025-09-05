package com.wes.truthdare.ui.navigation

import androidx.lifecycle.ViewModel
import com.wes.truthdare.core.data.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for accessing app preferences
 */
@HiltViewModel
class AppPreferencesViewModel @Inject constructor(
    val appPreferences: AppPreferences
) : ViewModel()