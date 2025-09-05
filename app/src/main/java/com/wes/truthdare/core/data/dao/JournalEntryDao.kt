package com.wes.truthdare.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wes.truthdare.core.data.entities.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for journal entry operations
 */
@Dao
interface JournalEntryDao {
    /**
     * Get all journal entries
     * @return Flow of all journal entries
     */
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntryEntity>>
    
    /**
     * Get a journal entry by ID
     * @param id The entry ID
     * @return The journal entry, or null if not found
     */
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: String): JournalEntryEntity?
    
    /**
     * Get journal entries for a session
     * @param sessionId The session ID
     * @return Flow of journal entries for the session
     */
    @Query("SELECT * FROM journal_entries WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    fun getEntriesForSession(sessionId: String): Flow<List<JournalEntryEntity>>
    
    /**
     * Get journal entries for a player
     * @param playerId The player ID
     * @return Flow of journal entries for the player
     */
    @Query("SELECT * FROM journal_entries WHERE :playerId IN (playerIds) ORDER BY createdAt DESC")
    fun getEntriesForPlayer(playerId: String): Flow<List<JournalEntryEntity>>
    
    /**
     * Get journal entries with specific tags
     * @param tag The tag to search for
     * @return Flow of journal entries with the specified tag
     */
    @Query("SELECT * FROM journal_entries WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getEntriesWithTag(tag: String): Flow<List<JournalEntryEntity>>
    
    /**
     * Insert a journal entry
     * @param entry The journal entry to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)
    
    /**
     * Update a journal entry
     * @param entry The journal entry to update
     */
    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)
    
    /**
     * Delete a journal entry
     * @param entry The journal entry to delete
     */
    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)
    
    /**
     * Delete all journal entries
     */
    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()
}