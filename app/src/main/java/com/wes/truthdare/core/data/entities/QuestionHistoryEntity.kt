package com.wes.truthdare.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a question asked during a game session
 */
@Entity(
    tableName = "question_history",
    indices = [
        Index("sessionId"),
        Index("playerId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = GameSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuestionHistoryEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val playerId: String,
    val questionId: String,
    val questionType: String,
    val questionCategory: String,
    val questionText: String,
    val askedAt: Long,
    val wasSkipped: Boolean,
    val wasStarred: Boolean,
    val followUpId: String?,
    val tags: List<String>
)