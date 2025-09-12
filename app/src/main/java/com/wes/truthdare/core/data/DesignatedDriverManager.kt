package com.wes.truthdare.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for designated driver functionality
 */
@Singleton
class DesignatedDriverManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.driverDataStore: DataStore<Preferences> by preferencesDataStore(name = "designated_driver")
    
    companion object {
        private val DESIGNATED_DRIVER_ID = stringPreferencesKey("designated_driver_id")
    }
    
    /**
     * Get the current designated driver ID
     */
    val designatedDriverId: Flow<String?> = context.driverDataStore.data
        .map { preferences ->
            preferences[DESIGNATED_DRIVER_ID]
        }
    
    /**
     * Set the designated driver
     * @param playerId The ID of the player who is the designated driver
     */
    suspend fun setDesignatedDriver(playerId: String?) {
        context.driverDataStore.edit { preferences ->
            if (playerId != null) {
                preferences[DESIGNATED_DRIVER_ID] = playerId
            } else {
                preferences.remove(DESIGNATED_DRIVER_ID)
            }
        }
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
        return designatedDriverId.map { id ->
            id == playerId
        }
    }
}