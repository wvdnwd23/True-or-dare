package com.wes.truthdare.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wes.truthdare.core.selector.GameMode

/**
 * Entity representing a game session
 */
@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey
    val id: String,
    val startedAt: Long,
    val endedAt: Long?,
    val mode: GameMode,
    val playerIds: List<String>,
    val categories: List<String>,
    val maxHeat: Int,
    val maxDepth: Int
)