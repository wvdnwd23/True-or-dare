package com.wes.truthdare.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.IntensityLevel
import com.wes.truthdare.core.data.converters.RoomTypeConverters
import java.util.UUID

/**
 * Entity representing a game session with multi-select category support
 */
@Entity(tableName = "game_sessions_v2")
@TypeConverters(RoomTypeConverters::class)
data class GameSessionEntityV2(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val playerIds: List<String>,
    val categoryIds: List<String>,
    val subOptionIds: List<String>,
    val maxIntensityLevel: IntensityLevel,
    val ageVerified: Boolean,
    val traumaHealingOptedIn: Boolean,
    val gameMode: GameMode = GameMode.CLASSIC, // For backward compatibility
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val roundCount: Int = 0,
    val isActive: Boolean = true
) {
    /**
     * Check if the session has ended
     * @return True if the session has ended
     */
    fun hasEnded(): Boolean {
        return endedAt != null
    }
    
    /**
     * Get the duration of the session in milliseconds
     * @return The duration of the session, or null if the session is still active
     */
    fun getDuration(): Long? {
        return endedAt?.let { it - startedAt }
    }
    
    /**
     * Convert to the original GameSessionEntity format
     * @return A GameSessionEntity object
     */
    fun toGameSessionEntity(): GameSessionEntity {
        return GameSessionEntity(
            id = id,
            playerIds = playerIds,
            mode = gameMode.name,
            categories = categoryIds,
            maxHeat = when (maxIntensityLevel) {
                IntensityLevel.SOFT -> 33
                IntensityLevel.MEDIUM -> 66
                IntensityLevel.HOT -> 100
            },
            maxDepth = 3, // Default value
            startedAt = startedAt,
            endedAt = endedAt,
            roundCount = roundCount,
            isActive = isActive
        )
    }
    
    companion object {
        /**
         * Create a new game session entity from the original GameSessionEntity
         * @param entity The original GameSessionEntity
         * @param categoryIds The category IDs (defaults to entity.categories)
         * @param subOptionIds The sub-option IDs (defaults to empty list)
         * @param maxIntensityLevel The maximum intensity level (defaults to MEDIUM)
         * @param ageVerified True if age is verified (defaults to false)
         * @param traumaHealingOptedIn True if trauma healing is opted in (defaults to false)
         * @return A GameSessionEntityV2 object
         */
        fun fromGameSessionEntity(
            entity: GameSessionEntity,
            categoryIds: List<String> = entity.categories,
            subOptionIds: List<String> = emptyList(),
            maxIntensityLevel: IntensityLevel = IntensityLevel.MEDIUM,
            ageVerified: Boolean = false,
            traumaHealingOptedIn: Boolean = false
        ): GameSessionEntityV2 {
            return GameSessionEntityV2(
                id = entity.id,
                playerIds = entity.playerIds,
                categoryIds = categoryIds,
                subOptionIds = subOptionIds,
                maxIntensityLevel = maxIntensityLevel,
                ageVerified = ageVerified,
                traumaHealingOptedIn = traumaHealingOptedIn,
                gameMode = try {
                    GameMode.valueOf(entity.mode)
                } catch (e: Exception) {
                    GameMode.CLASSIC
                },
                startedAt = entity.startedAt,
                endedAt = entity.endedAt,
                roundCount = entity.roundCount,
                isActive = entity.isActive
            )
        }
    }
}