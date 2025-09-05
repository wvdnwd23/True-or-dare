package com.wes.truthdare.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wes.truthdare.core.data.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for player operations
 */
@Dao
interface PlayerDao {
    /**
     * Get all players
     * @return Flow of all players
     */
    @Query("SELECT * FROM players ORDER BY lastPlayedAt DESC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>
    
    /**
     * Get a player by ID
     * @param id The player ID
     * @return The player, or null if not found
     */
    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: String): PlayerEntity?
    
    /**
     * Get players by IDs
     * @param ids The player IDs
     * @return List of players
     */
    @Query("SELECT * FROM players WHERE id IN (:ids)")
    suspend fun getPlayersByIds(ids: List<String>): List<PlayerEntity>
    
    /**
     * Insert a player
     * @param player The player to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)
    
    /**
     * Insert multiple players
     * @param players The players to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)
    
    /**
     * Update a player
     * @param player The player to update
     */
    @Update
    suspend fun updatePlayer(player: PlayerEntity)
    
    /**
     * Delete a player
     * @param player The player to delete
     */
    @Delete
    suspend fun deletePlayer(player: PlayerEntity)
    
    /**
     * Delete all players
     */
    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()
    
    /**
     * Update a player's last played timestamp
     * @param playerId The player ID
     * @param timestamp The timestamp
     */
    @Query("UPDATE players SET lastPlayedAt = :timestamp WHERE id = :playerId")
    suspend fun updatePlayerLastPlayed(playerId: String, timestamp: Long)
}