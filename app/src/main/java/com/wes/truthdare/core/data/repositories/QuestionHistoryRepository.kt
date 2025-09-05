package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.agents.Question
import com.wes.truthdare.core.data.dao.QuestionHistoryDao
import com.wes.truthdare.core.data.entities.QuestionHistoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for question history operations
 */
@Singleton
class QuestionHistoryRepository @Inject constructor(
    private val questionHistoryDao: QuestionHistoryDao
) {
    /**
     * Get question history for a session
     * @param sessionId The session ID
     * @return Flow of question history for the session
     */
    fun getQuestionHistoryForSession(sessionId: String): Flow<List<QuestionHistoryEntity>> {
        return questionHistoryDao.getQuestionHistoryForSession(sessionId)
    }
    
    /**
     * Get question history for a player
     * @param playerId The player ID
     * @return Flow of question history for the player
     */
    fun getQuestionHistoryForPlayer(playerId: String): Flow<List<QuestionHistoryEntity>> {
        return questionHistoryDao.getQuestionHistoryForPlayer(playerId)
    }
    
    /**
     * Get recent questions for a player
     * @param playerId The player ID
     * @param limit The maximum number of questions to return
     * @return List of recent questions for the player
     */
    suspend fun getRecentQuestionsForPlayer(playerId: String, limit: Int): List<QuestionHistoryEntity> {
        return questionHistoryDao.getRecentQuestionsForPlayer(playerId, limit)
    }
    
    /**
     * Get starred questions for a player
     * @param playerId The player ID
     * @return Flow of starred questions for the player
     */
    fun getStarredQuestionsForPlayer(playerId: String): Flow<List<QuestionHistoryEntity>> {
        return questionHistoryDao.getStarredQuestionsForPlayer(playerId)
    }
    
    /**
     * Get questions with specific tags
     * @param tag The tag to search for
     * @return List of questions with the specified tag
     */
    suspend fun getQuestionsWithTag(tag: String): List<QuestionHistoryEntity> {
        return questionHistoryDao.getQuestionsWithTag(tag)
    }
    
    /**
     * Record a question being asked
     * @param sessionId The session ID
     * @param playerId The player ID
     * @param question The question that was asked
     * @param wasSkipped Whether the question was skipped
     * @return The created question history entry
     */
    suspend fun recordQuestion(
        sessionId: String,
        playerId: String,
        question: Question,
        wasSkipped: Boolean
    ): QuestionHistoryEntity {
        val questionHistory = QuestionHistoryEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            playerId = playerId,
            questionId = question.id,
            questionType = question.type,
            questionCategory = question.category,
            questionText = question.text,
            askedAt = System.currentTimeMillis(),
            wasSkipped = wasSkipped,
            wasStarred = false,
            followUpId = null,
            tags = question.tags
        )
        questionHistoryDao.insertQuestionHistory(questionHistory)
        return questionHistory
    }
    
    /**
     * Record a follow-up question
     * @param originalQuestionId The original question ID
     * @param followUpQuestion The follow-up question
     * @param sessionId The session ID
     * @param playerId The player ID
     * @return The created question history entry for the follow-up
     */
    suspend fun recordFollowUpQuestion(
        originalQuestionId: String,
        followUpQuestion: Question,
        sessionId: String,
        playerId: String
    ): QuestionHistoryEntity {
        val followUpHistory = QuestionHistoryEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            playerId = playerId,
            questionId = followUpQuestion.id,
            questionType = followUpQuestion.type,
            questionCategory = followUpQuestion.category,
            questionText = followUpQuestion.text,
            askedAt = System.currentTimeMillis(),
            wasSkipped = false,
            wasStarred = false,
            followUpId = null,
            tags = followUpQuestion.tags
        )
        questionHistoryDao.insertQuestionHistory(followUpHistory)
        
        // Link the original question to the follow-up
        questionHistoryDao.setQuestionFollowUp(originalQuestionId, followUpHistory.id)
        
        return followUpHistory
    }
    
    /**
     * Star a question
     * @param questionId The question ID
     * @param isStarred Whether the question is starred
     */
    suspend fun starQuestion(questionId: String, isStarred: Boolean) {
        questionHistoryDao.setQuestionStarred(questionId, isStarred)
    }
    
    /**
     * Delete all question history
     */
    suspend fun deleteAllQuestionHistory() {
        questionHistoryDao.deleteAllQuestionHistory()
    }
}