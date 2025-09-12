package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.IntensityLevel
import com.wes.truthdare.core.data.entities.GameSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing game sessions with multi-select category support
 */
interface GameSessionRepositoryV2 {
    /**
     * Get all game sessions
     * @return List of all game sessions
     */
    suspend fun getAllSessions(): List<GameSessionEntity>
    
    /**
     * Get a game session by ID
     * @param sessionId The session ID
     * @return The game session, or null if not found
     */
    suspend fun getSessionById(sessionId: String): GameSessionEntity?
    
    /**
     * Get game sessions as a Flow
     * @return Flow of all game sessions
     */
    fun getSessionsFlow(): Flow<List<GameSessionEntity>>
    
    /**
     * Create a new game session with multi-select categories
     * @param playerIds The player IDs
     * @param categoryIds The category IDs
     * @param subOptionIds The sub-option IDs
     * @param maxIntensityLevel The maximum intensity level
     * @param ageVerified True if age is verified
     * @param traumaHealingOptedIn True if trauma healing is opted in
     * @return The created game session
     */
    suspend fun createMultiSelectSession(
        playerIds: List<String>,
        categoryIds: List<String>,
        subOptionIds: List<String>,
        maxIntensityLevel: IntensityLevel,
        ageVerified: Boolean = false,
        traumaHealingOptedIn: Boolean = false
    ): GameSessionEntity
    
    /**
     * Create a new game session with legacy parameters
     * @param playerIds The player IDs
     * @param mode The game mode
     * @param categories The categories
     * @param maxHeat The maximum heat level
     * @param maxDepth The maximum depth level
     * @return The created game session
     */
    suspend fun createSession(
        playerIds: List<String>,
        mode: com.wes.truthdare.core.selector.GameMode,
        categories: List<String>,
        maxHeat: Int,
        maxDepth: Int
    ): GameSessionEntity
    
    /**
     * Update a game session
     * @param session The game session to update
     */
    suspend fun updateSession(session: GameSessionEntity)
    
    /**
     * Delete a game session
     * @param sessionId The ID of the game session to delete
     */
    suspend fun deleteSession(sessionId: String)
    
    /**
     * End a game session
     * @param sessionId The ID of the game session to end
     */
    suspend fun endSession(sessionId: String)
    
    /**
     * Get the current game session ID
     * @return The current game session ID, or null if no session is active
     */
    suspend fun getCurrentSessionId(): String?
    
    /**
     * Set the current game session ID
     * @param sessionId The game session ID to set as current
     */
    suspend fun setCurrentSessionId(sessionId: String)
}