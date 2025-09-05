package com.wes.truthdare.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a player's preferences and learning data
 */
@Entity(
    tableName = "player_preferences",
    indices = [Index("playerId")],
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlayerPreferenceEntity(
    @PrimaryKey
    val id: String,
    val playerId: String,
    val tagWeights: Map<String, Float>,
    val depthComfort: Int,
    val heatComfort: Int,
    val favoriteCategories: List<String>,
    val avoidedCategories: List<String>,
    val lastUpdated: Long
)