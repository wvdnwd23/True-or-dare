package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.ContentFilterLevel
import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.IntensityLevel
import com.wes.truthdare.core.data.models.EnhancedQuestionV2
import com.wes.truthdare.core.data.models.QuestionType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing questions with multi-select category support
 */
interface QuestionRepositoryV2 {
    /**
     * Get all questions
     * @return List of all questions
     */
    suspend fun getAllQuestions(): List<EnhancedQuestionV2>
    
    /**
     * Get questions for specific categories
     * @param categoryIds The category IDs
     * @return List of questions for the categories
     */
    suspend fun getQuestionsForCategories(categoryIds: List<String>): List<EnhancedQuestionV2>
    
    /**
     * Get questions for specific sub-options
     * @param subOptionIds The sub-option IDs
     * @return List of questions for the sub-options
     */
    suspend fun getQuestionsForSubOptions(subOptionIds: List<String>): List<EnhancedQuestionV2>
    
    /**
     * Get questions for specific categories and sub-options
     * @param categoryIds The category IDs
     * @param subOptionIds The sub-option IDs
     * @return List of questions for the categories and sub-options
     */
    suspend fun getQuestionsForCategoriesAndSubOptions(
        categoryIds: List<String>,
        subOptionIds: List<String>
    ): List<EnhancedQuestionV2>
    
    /**
     * Get questions filtered by intensity level
     * @param maxIntensityLevel The maximum intensity level
     * @return List of questions with intensity level less than or equal to the specified level
     */
    suspend fun getQuestionsFilteredByIntensity(maxIntensityLevel: IntensityLevel): List<EnhancedQuestionV2>
    
    /**
     * Get questions filtered by content filter level
     * @param maxContentFilterLevel The maximum content filter level
     * @return List of questions with content filter level less than or equal to the specified level
     */
    suspend fun getQuestionsFilteredByContentFilter(maxContentFilterLevel: ContentFilterLevel): List<EnhancedQuestionV2>
    
    /**
     * Get questions filtered by type
     * @param type The question type
     * @return List of questions of the specified type
     */
    suspend fun getQuestionsFilteredByType(type: QuestionType): List<EnhancedQuestionV2>
    
    /**
     * Get questions filtered by game mode
     * @param gameMode The game mode
     * @return List of questions compatible with the specified game mode
     */
    suspend fun getQuestionsFilteredByGameMode(gameMode: GameMode): List<EnhancedQuestionV2>
    
    /**
     * Get questions with combined filters
     * @param categoryIds The category IDs
     * @param subOptionIds The sub-option IDs
     * @param maxIntensityLevel The maximum intensity level
     * @param maxContentFilterLevel The maximum content filter level
     * @param type The question type (optional)
     * @param gameMode The game mode (optional)
     * @return List of questions matching all filters
     */
    suspend fun getQuestionsWithFilters(
        categoryIds: List<String>,
        subOptionIds: List<String>,
        maxIntensityLevel: IntensityLevel,
        maxContentFilterLevel: ContentFilterLevel,
        type: QuestionType? = null,
        gameMode: GameMode? = null
    ): List<EnhancedQuestionV2>
    
    /**
     * Get sample questions for preview
     * @param categoryIds The category IDs
     * @param subOptionIds The sub-option IDs
     * @param maxIntensityLevel The maximum intensity level
     * @param count The number of sample questions to return
     * @return List of sample question texts
     */
    suspend fun getSampleQuestionsForPreview(
        categoryIds: List<String>,
        subOptionIds: List<String>,
        maxIntensityLevel: IntensityLevel,
        count: Int = 5
    ): List<String>
    
    /**
     * Insert or update a question
     * @param question The question to insert or update
     * @return The ID of the inserted or updated question
     */
    suspend fun insertOrUpdateQuestion(question: EnhancedQuestionV2): String
    
    /**
     * Insert or update multiple questions
     * @param questions The questions to insert or update
     */
    suspend fun insertOrUpdateQuestions(questions: List<EnhancedQuestionV2>)
    
    /**
     * Delete a question
     * @param questionId The ID of the question to delete
     */
    suspend fun deleteQuestion(questionId: String)
    
    /**
     * Get the count of questions for a specific category
     * @param categoryId The category ID
     * @return The number of questions for the category
     */
    suspend fun getQuestionCountForCategory(categoryId: String): Int
    
    /**
     * Get the count of questions for a specific sub-option
     * @param subOptionId The sub-option ID
     * @return The number of questions for the sub-option
     */
    suspend fun getQuestionCountForSubOption(subOptionId: String): Int
    
    /**
     * Update question counts for all categories and sub-options
     */
    suspend fun updateAllQuestionCounts()
}