package com.wes.truthdare.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for designated driver functionality in drinking games
 */
@Singleton
class DesignatedDriverManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val DESIGNATED_DRIVER_KEY = stringPreferencesKey("designated_driver_id")
    }
    
    // In-memory cache of the designated driver ID
    private val _designatedDriverId = MutableStateFlow<String?>(null)
    val designatedDriverId: StateFlow<String?> = _designatedDriverId
    
    init {
        // Load the designated driver ID from DataStore
        dataStore.data.map { preferences ->
            preferences[DESIGNATED_DRIVER_KEY]
        }.collect { driverId ->
            _designatedDriverId.value = driverId
        }
    }
    
    /**
     * Set the designated driver
     * @param playerId The ID of the player who is the designated driver
     */
    suspend fun setDesignatedDriver(playerId: String?) {
        dataStore.edit { preferences ->
            if (playerId != null) {
                preferences[DESIGNATED_DRIVER_KEY] = playerId
            } else {
                preferences.remove(DESIGNATED_DRIVER_KEY)
            }
        }
        _designatedDriverId.value = playerId
    }
    
    /**
     * Clear the designated driver
     */
    suspend fun clearDesignatedDriver() {
        setDesignatedDriver(null)
    }
    
    /**
     * Check if a player is the designated driver
     * @param playerId The ID of the player to check
     * @return Flow of Boolean indicating if the player is the designated driver
     */
    fun isDesignatedDriver(playerId: String): Flow<Boolean> {
        return designatedDriverId.map { it == playerId }
    }
}