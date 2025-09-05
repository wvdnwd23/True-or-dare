package com.wes.truthdare.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a player in the game
 */
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatarColor: Int,
    val createdAt: Long,
    val lastPlayedAt: Long
)