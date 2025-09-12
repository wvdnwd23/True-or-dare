package com.wes.truthdare.core.data.models

import com.wes.truthdare.core.data.ContentFilterLevel
import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.IntensityLevel
import java.util.UUID

/**
 * Enhanced question model with support for multi-select categories and sub-options
 */
data class EnhancedQuestionV2(
    val id: String = UUID.randomUUID().toString(),
    val type: QuestionType,
    val text: String,
    val categoryId: String,
    val subOptionIds: List<String> = emptyList(),
    val targets: TargetType,
    val difficulty: QuestionDifficulty,
    val contentFilterLevel: ContentFilterLevel,
    val intensityLevel: IntensityLevel = IntensityLevel.MEDIUM,
    val tags: List<String> = emptyList(),
    val isUserGenerated: Boolean = false,
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val compatibleGameModes: List<GameMode> = listOf(GameMode.CLASSIC),
    val depthLevel: Int? = null,
    val isDaily: Boolean = false,
    val isPremium: Boolean = false
) {
    /**
     * Check if this question belongs to a specific category
     * @param categoryId The category ID to check
     * @return True if the question belongs to the category
     */
    fun belongsToCategory(categoryId: String): Boolean {
        return this.categoryId == categoryId
    }
    
    /**
     * Check if this question belongs to any of the specified sub-options
     * @param subOptionIds The sub-option IDs to check
     * @return True if the question belongs to any of the sub-options
     */
    fun belongsToAnySubOption(subOptionIds: List<String>): Boolean {
        return subOptionIds.isEmpty() || this.subOptionIds.any { it in subOptionIds }
    }
    
    /**
     * Check if this question's intensity level is allowed based on the maximum allowed level
     * @param maxIntensityLevel The maximum allowed intensity level
     * @return True if the question's intensity level is allowed
     */
    fun isIntensityAllowed(maxIntensityLevel: IntensityLevel): Boolean {
        return when (maxIntensityLevel) {
            IntensityLevel.SOFT -> intensityLevel == IntensityLevel.SOFT
            IntensityLevel.MEDIUM -> intensityLevel == IntensityLevel.SOFT || intensityLevel == IntensityLevel.MEDIUM
            IntensityLevel.HOT -> true
        }
    }
    
    /**
     * Check if this question is age-appropriate based on the specified age
     * @param age The age to check against
     * @return True if the question is appropriate for the specified age
     */
    fun isAgeAppropriate(age: Int): Boolean {
        return when (contentFilterLevel) {
            ContentFilterLevel.LOW -> true
            ContentFilterLevel.MEDIUM -> age >= 13
            ContentFilterLevel.HIGH -> age >= 18
            ContentFilterLevel.EXTREME -> age >= 18
        }
    }
    
    /**
     * Convert this EnhancedQuestionV2 to the original EnhancedQuestion format
     * @return An EnhancedQuestion object
     */
    fun toEnhancedQuestion(): EnhancedQuestion {
        return EnhancedQuestion(
            id = id,
            type = type,
            text = text,
            category = categoryId, // Use categoryId as the category string
            targets = targets,
            difficulty = difficulty,
            contentFilterLevel = contentFilterLevel,
            tags = tags + subOptionIds, // Include sub-option IDs as tags for backward compatibility
            isUserGenerated = isUserGenerated,
            createdBy = createdBy,
            createdAt = createdAt,
            compatibleGameModes = compatibleGameModes,
            depthLevel = depthLevel,
            isDaily = isDaily,
            isPremium = isPremium
        )
    }
    
    companion object {
        /**
         * Convert an EnhancedQuestion to EnhancedQuestionV2
         * @param question The original EnhancedQuestion
         * @param categoryId The category ID (defaults to the original category string)
         * @param subOptionIds The sub-option IDs (defaults to empty list)
         * @param intensityLevel The intensity level (defaults to MEDIUM)
         * @return An EnhancedQuestionV2 object
         */
        fun fromEnhancedQuestion(
            question: EnhancedQuestion,
            categoryId: String = question.category,
            subOptionIds: List<String> = emptyList(),
            intensityLevel: IntensityLevel = IntensityLevel.MEDIUM
        ): EnhancedQuestionV2 {
            return EnhancedQuestionV2(
                id = question.id,
                type = question.type,
                text = question.text,
                categoryId = categoryId,
                subOptionIds = subOptionIds,
                targets = question.targets,
                difficulty = question.difficulty,
                contentFilterLevel = question.contentFilterLevel,
                intensityLevel = intensityLevel,
                tags = question.tags,
                isUserGenerated = question.isUserGenerated,
                createdBy = question.createdBy,
                createdAt = question.createdAt,
                compatibleGameModes = question.compatibleGameModes,
                depthLevel = question.depthLevel,
                isDaily = question.isDaily,
                isPremium = question.isPremium
            )
        }
    }
}