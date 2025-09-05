package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.dao.PlayerDao
import com.wes.truthdare.core.data.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for player operations
 */
@Singleton
class PlayerRepository @Inject constructor(
    private val playerDao: PlayerDao
) {
    /**
     * Get all players
     * @return Flow of all players
     */
    fun getAllPlayers(): Flow<List<PlayerEntity>> {
        return playerDao.getAllPlayers()
    }
    
    /**
     * Get a player by ID
     * @param id The player ID
     * @return The player, or null if not found
     */
    suspend fun getPlayerById(id: String): PlayerEntity? {
        return playerDao.getPlayerById(id)
    }
    
    /**
     * Get players by IDs
     * @param ids The player IDs
     * @return List of players
     */
    suspend fun getPlayersByIds(ids: List<String>): List<PlayerEntity> {
        return playerDao.getPlayersByIds(ids)
    }
    
    /**
     * Create a new player
     * @param name The player name
     * @param avatarColor The player avatar color
     * @return The created player
     */
    suspend fun createPlayer(name: String, avatarColor: Int): PlayerEntity {
        val now = System.currentTimeMillis()
        val player = PlayerEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            avatarColor = avatarColor,
            createdAt = now,
            lastPlayedAt = now
        )
        playerDao.insertPlayer(player)
        return player
    }
    
    /**
     * Update a player
     * @param player The player to update
     */
    suspend fun updatePlayer(player: PlayerEntity) {
        playerDao.updatePlayer(player)
    }
    
    /**
     * Delete a player
     * @param player The player to delete
     */
    suspend fun deletePlayer(player: PlayerEntity) {
        playerDao.deletePlayer(player)
    }
    
    /**
     * Update a player's last played timestamp
     * @param playerId The player ID
     */
    suspend fun updatePlayerLastPlayed(playerId: String) {
        playerDao.updatePlayerLastPlayed(playerId, System.currentTimeMillis())
    }
    
    /**
     * Delete all players
     */
    suspend fun deleteAllPlayers() {
        playerDao.deleteAllPlayers()
    }
}