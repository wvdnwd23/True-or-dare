package com.wes.truthdare.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wes.truthdare.core.data.entities.QuestionHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for question history operations
 */
@Dao
interface QuestionHistoryDao {
    /**
     * Get question history for a session
     * @param sessionId The session ID
     * @return Flow of question history for the session
     */
    @Query("SELECT * FROM question_history WHERE sessionId = :sessionId ORDER BY askedAt ASC")
    fun getQuestionHistoryForSession(sessionId: String): Flow<List<QuestionHistoryEntity>>
    
    /**
     * Get question history for a player
     * @param playerId The player ID
     * @return Flow of question history for the player
     */
    @Query("SELECT * FROM question_history WHERE playerId = :playerId ORDER BY askedAt DESC")
    fun getQuestionHistoryForPlayer(playerId: String): Flow<List<QuestionHistoryEntity>>
    
    /**
     * Get recent questions for a player
     * @param playerId The player ID
     * @param limit The maximum number of questions to return
     * @return List of recent questions for the player
     */
    @Query("SELECT * FROM question_history WHERE playerId = :playerId ORDER BY askedAt DESC LIMIT :limit")
    suspend fun getRecentQuestionsForPlayer(playerId: String, limit: Int): List<QuestionHistoryEntity>
    
    /**
     * Get starred questions for a player
     * @param playerId The player ID
     * @return Flow of starred questions for the player
     */
    @Query("SELECT * FROM question_history WHERE playerId = :playerId AND wasStarred = 1 ORDER BY askedAt DESC")
    fun getStarredQuestionsForPlayer(playerId: String): Flow<List<QuestionHistoryEntity>>
    
    /**
     * Get questions with specific tags
     * @param tags The tags to search for
     * @return List of questions with the specified tags
     */
    @Query("SELECT * FROM question_history WHERE tags LIKE '%' || :tag || '%' ORDER BY askedAt DESC")
    suspend fun getQuestionsWithTag(tag: String): List<QuestionHistoryEntity>
    
    /**
     * Insert a question history entry
     * @param questionHistory The question history entry to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionHistory(questionHistory: QuestionHistoryEntity)
    
    /**
     * Insert multiple question history entries
     * @param questionHistories The question history entries to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionHistories(questionHistories: List<QuestionHistoryEntity>)
    
    /**
     * Update a question history entry
     * @param questionHistory The question history entry to update
     */
    @Update
    suspend fun updateQuestionHistory(questionHistory: QuestionHistoryEntity)
    
    /**
     * Set a question as starred
     * @param questionId The question ID
     * @param isStarred Whether the question is starred
     */
    @Query("UPDATE question_history SET wasStarred = :isStarred WHERE id = :questionId")
    suspend fun setQuestionStarred(questionId: String, isStarred: Boolean)
    
    /**
     * Set a follow-up question for a question
     * @param questionId The question ID
     * @param followUpId The follow-up question ID
     */
    @Query("UPDATE question_history SET followUpId = :followUpId WHERE id = :questionId")
    suspend fun setQuestionFollowUp(questionId: String, followUpId: String)
    
    /**
     * Delete all question history
     */
    @Query("DELETE FROM question_history")
    suspend fun deleteAllQuestionHistory()
}