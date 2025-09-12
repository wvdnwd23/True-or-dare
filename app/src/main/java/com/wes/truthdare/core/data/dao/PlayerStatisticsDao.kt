package com.wes.truthdare.core.data.dao

import androidx.room.*
import com.wes.truthdare.core.data.entities.AggregatedPlayerStatistics
import com.wes.truthdare.core.data.entities.PlayerStatisticsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for player statistics
 */
@Dao
interface PlayerStatisticsDao {
    /**
     * Get statistics for a player in a specific game mode
     */
    @Query("SELECT * FROM player_statistics WHERE playerId = :playerId AND gameMode = :gameMode")
    fun getPlayerStatistics(playerId: String, gameMode: String): Flow<PlayerStatisticsEntity?>
    
    /**
     * Get all statistics for a player across all game modes
     */
    @Query("SELECT * FROM player_statistics WHERE playerId = :playerId")
    fun getAllPlayerStatistics(playerId: String): Flow<List<PlayerStatisticsEntity>>
    
    /**
     * Get aggregated statistics for a player across all game modes
     */
    @Transaction
    @Query("""
        SELECT 
            p.id as playerId,
            p.name as playerName,
            p.avatarUri as avatarUri,
            SUM(ps.truthsAnswered) as totalTruthsAnswered,
            SUM(ps.daresCompleted) as totalDaresCompleted,
            SUM(ps.truthsSkipped) as totalTruthsSkipped,
            SUM(ps.daresSkipped) as totalDaresSkipped,
            SUM(ps.shotsTaken) as totalShotsTaken,
            SUM(ps.gamesPlayed) as totalGamesPlayed,
            SUM(ps.totalPlayTimeMs) as totalPlayTimeMs,
            MAX(ps.lastPlayedTimestamp) as lastPlayedTimestamp,
            MAX(ps.highestDifficultyReached) as highestDifficultyReached,
            MAX(ps.longestStreak) as longestStreak,
            MAX(ps.currentStreak) as currentStreak,
            (
                SELECT ps2.gameMode
                FROM player_statistics ps2
                WHERE ps2.playerId = p.id
                GROUP BY ps2.gameMode
                ORDER BY COUNT(*) DESC
                LIMIT 1
            ) as favoriteGameMode
        FROM players p
        LEFT JOIN player_statistics ps ON p.id = ps.playerId
        WHERE p.id = :playerId
        GROUP BY p.id
    """)
    fun getAggregatedPlayerStatistics(playerId: String): Flow<AggregatedPlayerStatistics?>
    
    /**
     * Get leaderboard of players by total questions answered
     */
    @Transaction
    @Query("""
        SELECT 
            p.id as playerId,
            p.name as playerName,
            p.avatarUri as avatarUri,
            SUM(ps.truthsAnswered) as totalTruthsAnswered,
            SUM(ps.daresCompleted) as totalDaresCompleted,
            SUM(ps.truthsSkipped) as totalTruthsSkipped,
            SUM(ps.daresSkipped) as totalDaresSkipped,
            SUM(ps.shotsTaken) as totalShotsTaken,
            SUM(ps.gamesPlayed) as totalGamesPlayed,
            SUM(ps.totalPlayTimeMs) as totalPlayTimeMs,
            MAX(ps.lastPlayedTimestamp) as lastPlayedTimestamp,
            MAX(ps.highestDifficultyReached) as highestDifficultyReached,
            MAX(ps.longestStreak) as longestStreak,
            MAX(ps.currentStreak) as currentStreak,
            (
                SELECT ps2.gameMode
                FROM player_statistics ps2
                WHERE ps2.playerId = p.id
                GROUP BY ps2.gameMode
                ORDER BY COUNT(*) DESC
                LIMIT 1
            ) as favoriteGameMode
        FROM players p
        LEFT JOIN player_statistics ps ON p.id = ps.playerId
        GROUP BY p.id
        ORDER BY (SUM(ps.truthsAnswered) + SUM(ps.daresCompleted)) DESC
        LIMIT :limit
    """)
    fun getLeaderboardByQuestionsAnswered(limit: Int = 10): Flow<List<AggregatedPlayerStatistics>>
    
    /**
     * Get leaderboard of players by games played
     */
    @Transaction
    @Query("""
        SELECT 
            p.id as playerId,
            p.name as playerName,
            p.avatarUri as avatarUri,
            SUM(ps.truthsAnswered) as totalTruthsAnswered,
            SUM(ps.daresCompleted) as totalDaresCompleted,
            SUM(ps.truthsSkipped) as totalTruthsSkipped,
            SUM(ps.daresSkipped) as totalDaresSkipped,
            SUM(ps.shotsTaken) as totalShotsTaken,
            SUM(ps.gamesPlayed) as totalGamesPlayed,
            SUM(ps.totalPlayTimeMs) as totalPlayTimeMs,
            MAX(ps.lastPlayedTimestamp) as lastPlayedTimestamp,
            MAX(ps.highestDifficultyReached) as highestDifficultyReached,
            MAX(ps.longestStreak) as longestStreak,
            MAX(ps.currentStreak) as currentStreak,
            (
                SELECT ps2.gameMode
                FROM player_statistics ps2
                WHERE ps2.playerId = p.id
                GROUP BY ps2.gameMode
                ORDER BY COUNT(*) DESC
                LIMIT 1
            ) as favoriteGameMode
        FROM players p
        LEFT JOIN player_statistics ps ON p.id = ps.playerId
        GROUP BY p.id
        ORDER BY SUM(ps.gamesPlayed) DESC
        LIMIT :limit
    """)
    fun getLeaderboardByGamesPlayed(limit: Int = 10): Flow<List<AggregatedPlayerStatistics>>
    
    /**
     * Get leaderboard of players by shots taken
     */
    @Transaction
    @Query("""
        SELECT 
            p.id as playerId,
            p.name as playerName,
            p.avatarUri as avatarUri,
            SUM(ps.truthsAnswered) as totalTruthsAnswered,
            SUM(ps.daresCompleted) as totalDaresCompleted,
            SUM(ps.truthsSkipped) as totalTruthsSkipped,
            SUM(ps.daresSkipped) as totalDaresSkipped,
            SUM(ps.shotsTaken) as totalShotsTaken,
            SUM(ps.gamesPlayed) as totalGamesPlayed,
            SUM(ps.totalPlayTimeMs) as totalPlayTimeMs,
            MAX(ps.lastPlayedTimestamp) as lastPlayedTimestamp,
            MAX(ps.highestDifficultyReached) as highestDifficultyReached,
            MAX(ps.longestStreak) as longestStreak,
            MAX(ps.currentStreak) as currentStreak,
            (
                SELECT ps2.gameMode
                FROM player_statistics ps2
                WHERE ps2.playerId = p.id
                GROUP BY ps2.gameMode
                ORDER BY COUNT(*) DESC
                LIMIT 1
            ) as favoriteGameMode
        FROM players p
        LEFT JOIN player_statistics ps ON p.id = ps.playerId
        GROUP BY p.id
        ORDER BY SUM(ps.shotsTaken) DESC
        LIMIT :limit
    """)
    fun getLeaderboardByShotsTaken(limit: Int = 10): Flow<List<AggregatedPlayerStatistics>>
    
    /**
     * Insert or update player statistics
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlayerStatistics(statistics: PlayerStatisticsEntity)
    
    /**
     * Increment truths answered for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET truthsAnswered = truthsAnswered + 1,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun incrementTruthsAnswered(playerId: String, gameMode: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Increment dares completed for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET daresCompleted = daresCompleted + 1,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun incrementDaresCompleted(playerId: String, gameMode: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Increment truths skipped for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET truthsSkipped = truthsSkipped + 1,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun incrementTruthsSkipped(playerId: String, gameMode: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Increment dares skipped for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET daresSkipped = daresSkipped + 1,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun incrementDaresSkipped(playerId: String, gameMode: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Increment shots taken for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET shotsTaken = shotsTaken + :count,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun incrementShotsTaken(playerId: String, gameMode: String, count: Int = 1, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Increment games played for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET gamesPlayed = gamesPlayed + 1,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun incrementGamesPlayed(playerId: String, gameMode: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Add play time for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET totalPlayTimeMs = totalPlayTimeMs + :timeMs,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun addPlayTime(playerId: String, gameMode: String, timeMs: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update highest difficulty reached for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET highestDifficultyReached = MAX(highestDifficultyReached, :difficulty),
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun updateHighestDifficulty(playerId: String, gameMode: String, difficulty: Int, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update streak for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET currentStreak = :currentStreak,
            longestStreak = MAX(longestStreak, :currentStreak),
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun updateStreak(playerId: String, gameMode: String, currentStreak: Int, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Reset streak for a player in a game mode
     */
    @Query("""
        UPDATE player_statistics 
        SET currentStreak = 0,
            lastPlayedTimestamp = :timestamp
        WHERE playerId = :playerId AND gameMode = :gameMode
    """)
    suspend fun resetStreak(playerId: String, gameMode: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Delete all statistics for a player
     */
    @Query("DELETE FROM player_statistics WHERE playerId = :playerId")
    suspend fun deletePlayerStatistics(playerId: String)
}