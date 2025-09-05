package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.dao.GameSessionDao
import com.wes.truthdare.core.data.entities.GameSessionEntity
import com.wes.truthdare.core.selector.GameMode
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for game session operations
 */
@Singleton
class GameSessionRepository @Inject constructor(
    private val gameSessionDao: GameSessionDao
) {
    /**
     * Get all game sessions
     * @return Flow of all game sessions
     */
    fun getAllSessions(): Flow<List<GameSessionEntity>> {
        return gameSessionDao.getAllSessions()
    }
    
    /**
     * Get a game session by ID
     * @param id The session ID
     * @return The game session, or null if not found
     */
    suspend fun getSessionById(id: String): GameSessionEntity? {
        return gameSessionDao.getSessionById(id)
    }
    
    /**
     * Get active game sessions (not ended)
     * @return List of active game sessions
     */
    suspend fun getActiveSessions(): List<GameSessionEntity> {
        return gameSessionDao.getActiveSessions()
    }
    
    /**
     * Get game sessions for a player
     * @param playerId The player ID
     * @return Flow of game sessions for the player
     */
    fun getSessionsForPlayer(playerId: String): Flow<List<GameSessionEntity>> {
        return gameSessionDao.getSessionsForPlayer(playerId)
    }
    
    /**
     * Create a new game session
     * @param playerIds The player IDs
     * @param mode The game mode
     * @param categories The selected categories
     * @param maxHeat The maximum heat level
     * @param maxDepth The maximum depth level
     * @return The created game session
     */
    suspend fun createSession(
        playerIds: List<String>,
        mode: GameMode,
        categories: List<String>,
        maxHeat: Int,
        maxDepth: Int
    ): GameSessionEntity {
        val session = GameSessionEntity(
            id = UUID.randomUUID().toString(),
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            mode = mode,
            playerIds = playerIds,
            categories = categories,
            maxHeat = maxHeat,
            maxDepth = maxDepth
        )
        gameSessionDao.insertSession(session)
        return session
    }
    
    /**
     * End a game session
     * @param sessionId The session ID
     */
    suspend fun endSession(sessionId: String) {
        gameSessionDao.endSession(sessionId, System.currentTimeMillis())
    }
    
    /**
     * Update a game session
     * @param session The game session to update
     */
    suspend fun updateSession(session: GameSessionEntity) {
        gameSessionDao.updateSession(session)
    }
    
    /**
     * Delete all game sessions
     */
    suspend fun deleteAllSessions() {
        gameSessionDao.deleteAllSessions()
    }
}