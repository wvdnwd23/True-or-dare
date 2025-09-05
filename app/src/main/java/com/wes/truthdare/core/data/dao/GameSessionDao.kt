package com.wes.truthdare.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wes.truthdare.core.data.entities.GameSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for game session operations
 */
@Dao
interface GameSessionDao {
    /**
     * Get all game sessions
     * @return Flow of all game sessions
     */
    @Query("SELECT * FROM game_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<GameSessionEntity>>
    
    /**
     * Get a game session by ID
     * @param id The session ID
     * @return The game session, or null if not found
     */
    @Query("SELECT * FROM game_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): GameSessionEntity?
    
    /**
     * Get active game sessions (not ended)
     * @return List of active game sessions
     */
    @Query("SELECT * FROM game_sessions WHERE endedAt IS NULL ORDER BY startedAt DESC")
    suspend fun getActiveSessions(): List<GameSessionEntity>
    
    /**
     * Get game sessions for a player
     * @param playerId The player ID
     * @return Flow of game sessions for the player
     */
    @Query("SELECT * FROM game_sessions WHERE :playerId IN (playerIds) ORDER BY startedAt DESC")
    fun getSessionsForPlayer(playerId: String): Flow<List<GameSessionEntity>>
    
    /**
     * Insert a game session
     * @param session The game session to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity)
    
    /**
     * Update a game session
     * @param session The game session to update
     */
    @Update
    suspend fun updateSession(session: GameSessionEntity)
    
    /**
     * End a game session
     * @param sessionId The session ID
     * @param endTime The end timestamp
     */
    @Query("UPDATE game_sessions SET endedAt = :endTime WHERE id = :sessionId")
    suspend fun endSession(sessionId: String, endTime: Long)
    
    /**
     * Delete all game sessions
     */
    @Query("DELETE FROM game_sessions")
    suspend fun deleteAllSessions()
}