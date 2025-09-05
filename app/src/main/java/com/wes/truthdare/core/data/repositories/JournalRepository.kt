package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.dao.JournalEntryDao
import com.wes.truthdare.core.data.entities.JournalEntryEntity
import com.wes.truthdare.core.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for journal entry operations
 */
@Singleton
class JournalRepository @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val cryptoManager: CryptoManager
) {
    /**
     * Get all journal entries
     * @return Flow of all journal entries
     */
    fun getAllEntries(): Flow<List<JournalEntryEntity>> {
        return journalEntryDao.getAllEntries()
    }
    
    /**
     * Get a journal entry by ID
     * @param id The entry ID
     * @return The journal entry, or null if not found
     */
    suspend fun getEntryById(id: String): JournalEntryEntity? {
        return journalEntryDao.getEntryById(id)
    }
    
    /**
     * Get journal entries for a session
     * @param sessionId The session ID
     * @return Flow of journal entries for the session
     */
    fun getEntriesForSession(sessionId: String): Flow<List<JournalEntryEntity>> {
        return journalEntryDao.getEntriesForSession(sessionId)
    }
    
    /**
     * Get journal entries for a player
     * @param playerId The player ID
     * @return Flow of journal entries for the player
     */
    fun getEntriesForPlayer(playerId: String): Flow<List<JournalEntryEntity>> {
        return journalEntryDao.getEntriesForPlayer(playerId)
    }
    
    /**
     * Get journal entries with specific tags
     * @param tag The tag to search for
     * @return Flow of journal entries with the specified tag
     */
    fun getEntriesWithTag(tag: String): Flow<List<JournalEntryEntity>> {
        return journalEntryDao.getEntriesWithTag(tag)
    }
    
    /**
     * Create a new journal entry
     * @param sessionId The session ID
     * @param title The entry title
     * @param content The entry content
     * @param playerIds The player IDs
     * @param tags The entry tags
     * @param encrypt Whether to encrypt the content
     * @return The created journal entry
     */
    suspend fun createEntry(
        sessionId: String,
        title: String,
        content: String,
        playerIds: List<String>,
        tags: List<String>,
        encrypt: Boolean
    ): JournalEntryEntity {
        val processedContent = if (encrypt) {
            cryptoManager.encryptString(content)
        } else {
            content
        }
        
        val entry = JournalEntryEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            title = title,
            content = processedContent,
            createdAt = System.currentTimeMillis(),
            isEncrypted = encrypt,
            playerIds = playerIds,
            tags = tags
        )
        
        journalEntryDao.insertEntry(entry)
        return entry
    }
    
    /**
     * Update a journal entry
     * @param entry The journal entry to update
     * @param newContent The new content
     * @param encrypt Whether to encrypt the content
     */
    suspend fun updateEntry(
        entry: JournalEntryEntity,
        newContent: String,
        encrypt: Boolean
    ) {
        val processedContent = if (encrypt) {
            cryptoManager.encryptString(newContent)
        } else {
            newContent
        }
        
        val updatedEntry = entry.copy(
            content = processedContent,
            isEncrypted = encrypt
        )
        
        journalEntryDao.updateEntry(updatedEntry)
    }
    
    /**
     * Get decrypted content for an entry
     * @param entry The journal entry
     * @return The decrypted content, or the original content if not encrypted
     */
    fun getDecryptedContent(entry: JournalEntryEntity): String {
        return if (entry.isEncrypted) {
            try {
                cryptoManager.decryptString(entry.content)
            } catch (e: Exception) {
                "Error decrypting content"
            }
        } else {
            entry.content
        }
    }
    
    /**
     * Delete a journal entry
     * @param entry The journal entry to delete
     */
    suspend fun deleteEntry(entry: JournalEntryEntity) {
        journalEntryDao.deleteEntry(entry)
    }
    
    /**
     * Delete all journal entries
     */
    suspend fun deleteAllEntries() {
        journalEntryDao.deleteAllEntries()
    }
}