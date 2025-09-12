package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.dao.PlayerStatisticsDao
import com.wes.truthdare.core.data.entities.AggregatedPlayerStatistics
import com.wes.truthdare.core.data.entities.PlayerStatisticsEntity
import com.wes.truthdare.core.data.models.QuestionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for player statistics
 */
@Singleton
class PlayerStatisticsRepository @Inject constructor(
    private val playerStatisticsDao: PlayerStatisticsDao
) {
    /**
     * Get statistics for a player in a specific game mode
     */
    fun getPlayerStatistics(playerId: String, gameMode: GameMode): Flow<PlayerStatisticsEntity?> {
        return playerStatisticsDao.getPlayerStatistics(playerId, gameMode.name)
    }
    
    /**
     * Get all statistics for a player across all game modes
     */
    fun getAllPlayerStatistics(playerId: String): Flow<List<PlayerStatisticsEntity>> {
        return playerStatisticsDao.getAllPlayerStatistics(playerId)
    }
    
    /**
     * Get aggregated statistics for a player across all game modes
     */
    fun getAggregatedPlayerStatistics(playerId: String): Flow<AggregatedPlayerStatistics?> {
        return playerStatisticsDao.getAggregatedPlayerStatistics(playerId).map { stats ->
            stats?.let {
                // Convert favoriteGameMode string to enum
                val favoriteGameMode = try {
                    if (it.favoriteGameMode != null) GameMode.valueOf(it.favoriteGameMode) else null
                } catch (e: Exception) {
                    null
                }
                
                // Return the stats with the converted favoriteGameMode
                it.copy(favoriteGameMode = favoriteGameMode)
            }
        }
    }
    
    /**
     * Get leaderboard of players by total questions answered
     */
    fun getLeaderboardByQuestionsAnswered(limit: Int = 10): Flow<List<AggregatedPlayerStatistics>> {
        return playerStatisticsDao.getLeaderboardByQuestionsAnswered(limit).map { statsList ->
            statsList.map { stats ->
                // Convert favoriteGameMode string to enum
                val favoriteGameMode = try {
                    if (stats.favoriteGameMode != null) GameMode.valueOf(stats.favoriteGameMode) else null
                } catch (e: Exception) {
                    null
                }
                
                // Return the stats with the converted favoriteGameMode
                stats.copy(favoriteGameMode = favoriteGameMode)
            }
        }
    }
    
    /**
     * Get leaderboard of players by games played
     */
    fun getLeaderboardByGamesPlayed(limit: Int = 10): Flow<List<AggregatedPlayerStatistics>> {
        return playerStatisticsDao.getLeaderboardByGamesPlayed(limit).map { statsList ->
            statsList.map { stats ->
                // Convert favoriteGameMode string to enum
                val favoriteGameMode = try {
                    if (stats.favoriteGameMode != null) GameMode.valueOf(stats.favoriteGameMode) else null
                } catch (e: Exception) {
                    null
                }
                
                // Return the stats with the converted favoriteGameMode
                stats.copy(favoriteGameMode = favoriteGameMode)
            }
        }
    }
    
    /**
     * Get leaderboard of players by shots taken
     */
    fun getLeaderboardByShotsTaken(limit: Int = 10): Flow<List<AggregatedPlayerStatistics>> {
        return playerStatisticsDao.getLeaderboardByShotsTaken(limit).map { statsList ->
            statsList.map { stats ->
                // Convert favoriteGameMode string to enum
                val favoriteGameMode = try {
                    if (stats.favoriteGameMode != null) GameMode.valueOf(stats.favoriteGameMode) else null
                } catch (e: Exception) {
                    null
                }
                
                // Return the stats with the converted favoriteGameMode
                stats.copy(favoriteGameMode = favoriteGameMode)
            }
        }
    }
    
    /**
     * Initialize statistics for a player in a game mode
     */
    suspend fun initializePlayerStatistics(playerId: String, gameMode: GameMode) {
        val statistics = PlayerStatisticsEntity.create(playerId, gameMode)
        playerStatisticsDao.insertOrUpdatePlayerStatistics(statistics)
    }
    
    /**
     * Record a question answered or skipped
     */
    suspend fun recordQuestionResult(
        playerId: String,
        gameMode: GameMode,
        questionType: QuestionType,
        completed: Boolean
    ) {
        // Ensure statistics exist for this player and game mode
        ensureStatisticsExist(playerId, gameMode)
        
        // Record the result
        when {
            questionType == QuestionType.TRUTH && completed -> {
                playerStatisticsDao.incrementTruthsAnswered(playerId, gameMode.name)
            }
            questionType == QuestionType.DARE && completed -> {
                playerStatisticsDao.incrementDaresCompleted(playerId, gameMode.name)
            }
            questionType == QuestionType.TRUTH && !completed -> {
                playerStatisticsDao.incrementTruthsSkipped(playerId, gameMode.name)
            }
            questionType == QuestionType.DARE && !completed -> {
                playerStatisticsDao.incrementDaresSkipped(playerId, gameMode.name)
            }
        }
        
        // Update streak
        if (completed) {
            // Get current statistics
            val currentStats = playerStatisticsDao.getPlayerStatistics(playerId, gameMode.name)
                .map { it ?: PlayerStatisticsEntity.create(playerId, gameMode) }
                .collect { stats ->
                    // Increment streak
                    playerStatisticsDao.updateStreak(
                        playerId,
                        gameMode.name,
                        stats.currentStreak + 1
                    )
                }
        } else {
            // Reset streak on skip
            playerStatisticsDao.resetStreak(playerId, gameMode.name)
        }
    }
    
    /**
     * Record shots taken
     */
    suspend fun recordShotsTaken(playerId: String, gameMode: GameMode, count: Int) {
        // Ensure statistics exist for this player and game mode
        ensureStatisticsExist(playerId, gameMode)
        
        // Record the shots
        playerStatisticsDao.incrementShotsTaken(playerId, gameMode.name, count)
    }
    
    /**
     * Record game played
     */
    suspend fun recordGamePlayed(playerId: String, gameMode: GameMode) {
        // Ensure statistics exist for this player and game mode
        ensureStatisticsExist(playerId, gameMode)
        
        // Record the game
        playerStatisticsDao.incrementGamesPlayed(playerId, gameMode.name)
    }
    
    /**
     * Record play time
     */
    suspend fun recordPlayTime(playerId: String, gameMode: GameMode, timeMs: Long) {
        // Ensure statistics exist for this player and game mode
        ensureStatisticsExist(playerId, gameMode)
        
        // Record the play time
        playerStatisticsDao.addPlayTime(playerId, gameMode.name, timeMs)
    }
    
    /**
     * Record difficulty reached
     */
    suspend fun recordDifficultyReached(playerId: String, gameMode: GameMode, difficulty: Int) {
        // Ensure statistics exist for this player and game mode
        ensureStatisticsExist(playerId, gameMode)
        
        // Record the difficulty
        playerStatisticsDao.updateHighestDifficulty(playerId, gameMode.name, difficulty)
    }
    
    /**
     * Delete all statistics for a player
     */
    suspend fun deletePlayerStatistics(playerId: String) {
        playerStatisticsDao.deletePlayerStatistics(playerId)
    }
    
    /**
     * Ensure statistics exist for a player in a game mode
     */
    private suspend fun ensureStatisticsExist(playerId: String, gameMode: GameMode) {
        val statistics = playerStatisticsDao.getPlayerStatistics(playerId, gameMode.name)
            .collect { stats ->
                if (stats == null) {
                    // Create new statistics
                    val newStats = PlayerStatisticsEntity.create(playerId, gameMode)
                    playerStatisticsDao.insertOrUpdatePlayerStatistics(newStats)
                }
            }
    }
}