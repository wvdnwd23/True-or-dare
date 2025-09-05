package com.wes.truthdare.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wes.truthdare.core.data.entities.PlayerPreferenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for player preference operations
 */
@Dao
interface PlayerPreferenceDao {
    /**
     * Get preferences for a player
     * @param playerId The player ID
     * @return Flow of preferences for the player
     */
    @Query("SELECT * FROM player_preferences WHERE playerId = :playerId")
    fun getPreferencesForPlayer(playerId: String): Flow<PlayerPreferenceEntity?>
    
    /**
     * Get preferences for a player (suspend function)
     * @param playerId The player ID
     * @return Preferences for the player, or null if not found
     */
    @Query("SELECT * FROM player_preferences WHERE playerId = :playerId")
    suspend fun getPreferencesForPlayerSync(playerId: String): PlayerPreferenceEntity?
    
    /**
     * Insert player preferences
     * @param preferences The preferences to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: PlayerPreferenceEntity)
    
    /**
     * Update player preferences
     * @param preferences The preferences to update
     */
    @Update
    suspend fun updatePreferences(preferences: PlayerPreferenceEntity)
    
    /**
     * Update tag weights for a player
     * @param playerId The player ID
     * @param tagWeights The tag weights as a JSON string
     * @param timestamp The update timestamp
     */
    @Query("UPDATE player_preferences SET tagWeights = :tagWeights, lastUpdated = :timestamp WHERE playerId = :playerId")
    suspend fun updateTagWeights(playerId: String, tagWeights: String, timestamp: Long)
    
    /**
     * Update depth comfort for a player
     * @param playerId The player ID
     * @param depthComfort The depth comfort level
     * @param timestamp The update timestamp
     */
    @Query("UPDATE player_preferences SET depthComfort = :depthComfort, lastUpdated = :timestamp WHERE playerId = :playerId")
    suspend fun updateDepthComfort(playerId: String, depthComfort: Int, timestamp: Long)
    
    /**
     * Update heat comfort for a player
     * @param playerId The player ID
     * @param heatComfort The heat comfort level
     * @param timestamp The update timestamp
     */
    @Query("UPDATE player_preferences SET heatComfort = :heatComfort, lastUpdated = :timestamp WHERE playerId = :playerId")
    suspend fun updateHeatComfort(playerId: String, heatComfort: Int, timestamp: Long)
    
    /**
     * Delete preferences for a player
     * @param playerId The player ID
     */
    @Query("DELETE FROM player_preferences WHERE playerId = :playerId")
    suspend fun deletePreferencesForPlayer(playerId: String)
    
    /**
     * Delete all player preferences
     */
    @Query("DELETE FROM player_preferences")
    suspend fun deleteAllPreferences()
}