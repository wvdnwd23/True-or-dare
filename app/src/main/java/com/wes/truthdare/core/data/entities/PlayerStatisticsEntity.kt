package com.wes.truthdare.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wes.truthdare.core.data.GameMode

/**
 * Entity for storing player statistics
 */
@Entity(
    tableName = "player_statistics",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId")]
)
data class PlayerStatisticsEntity(
    @PrimaryKey
    val id: String,
    val playerId: String,
    val gameMode: String, // Stored as string representation of GameMode enum
    val truthsAnswered: Int = 0,
    val daresCompleted: Int = 0,
    val truthsSkipped: Int = 0,
    val daresSkipped: Int = 0,
    val shotsTaken: Int = 0,
    val gamesPlayed: Int = 0,
    val totalPlayTimeMs: Long = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),
    val highestDifficultyReached: Int = 1,
    val longestStreak: Int = 0,
    val currentStreak: Int = 0
) {
    /**
     * Get the total number of questions answered (truths + dares)
     */
    fun getTotalQuestionsAnswered(): Int {
        return truthsAnswered + daresCompleted
    }
    
    /**
     * Get the total number of questions skipped (truths + dares)
     */
    fun getTotalQuestionsSkipped(): Int {
        return truthsSkipped + daresSkipped
    }
    
    /**
     * Get the completion rate (answered / (answered + skipped))
     */
    fun getCompletionRate(): Float {
        val total = getTotalQuestionsAnswered() + getTotalQuestionsSkipped()
        return if (total > 0) {
            getTotalQuestionsAnswered().toFloat() / total
        } else {
            0f
        }
    }
    
    /**
     * Get the game mode as an enum
     */
    fun getGameModeEnum(): GameMode {
        return try {
            GameMode.valueOf(gameMode)
        } catch (e: Exception) {
            GameMode.CLASSIC
        }
    }
    
    companion object {
        /**
         * Create a new player statistics entity
         */
        fun create(playerId: String, gameMode: GameMode): PlayerStatisticsEntity {
            return PlayerStatisticsEntity(
                id = "${playerId}_${gameMode.name}",
                playerId = playerId,
                gameMode = gameMode.name
            )
        }
    }
}

/**
 * Data class for aggregated player statistics across all game modes
 */
data class AggregatedPlayerStatistics(
    val playerId: String,
    val playerName: String,
    val avatarUri: String?,
    val totalTruthsAnswered: Int,
    val totalDaresCompleted: Int,
    val totalTruthsSkipped: Int,
    val totalDaresSkipped: Int,
    val totalShotsTaken: Int,
    val totalGamesPlayed: Int,
    val totalPlayTimeMs: Long,
    val lastPlayedTimestamp: Long,
    val highestDifficultyReached: Int,
    val longestStreak: Int,
    val currentStreak: Int,
    val favoriteGameMode: GameMode?
) {
    /**
     * Get the total number of questions answered (truths + dares)
     */
    fun getTotalQuestionsAnswered(): Int {
        return totalTruthsAnswered + totalDaresCompleted
    }
    
    /**
     * Get the total number of questions skipped (truths + dares)
     */
    fun getTotalQuestionsSkipped(): Int {
        return totalTruthsSkipped + totalDaresSkipped
    }
    
    /**
     * Get the completion rate (answered / (answered + skipped))
     */
    fun getCompletionRate(): Float {
        val total = getTotalQuestionsAnswered() + getTotalQuestionsSkipped()
        return if (total > 0) {
            getTotalQuestionsAnswered().toFloat() / total
        } else {
            0f
        }
    }
    
    /**
     * Get the total play time in hours
     */
    fun getTotalPlayTimeHours(): Float {
        return totalPlayTimeMs / (1000f * 60f * 60f)
    }
    
    /**
     * Get the formatted total play time (e.g., "2h 30m")
     */
    fun getFormattedTotalPlayTime(): String {
        val totalSeconds = totalPlayTimeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}