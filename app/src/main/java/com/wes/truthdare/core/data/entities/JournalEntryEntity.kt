package com.wes.truthdare.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a journal entry from a game session
 */
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val isEncrypted: Boolean,
    val playerIds: List<String>,
    val tags: List<String>
)