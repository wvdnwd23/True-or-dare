package com.wes.truthdare.core.data.models

import com.wes.truthdare.core.data.ContentFilterLevel
import com.wes.truthdare.core.data.GameMode
import java.util.UUID

/**
 * Enhanced question model with additional metadata for the upgraded app
 */
data class EnhancedQuestion(
    val id: String = UUID.randomUUID().toString(),
    val type: QuestionType,
    val text: String,
    val category: String,
    val targets: TargetType,
    val difficulty: QuestionDifficulty,
    val contentFilterLevel: ContentFilterLevel,
    val tags: List<String> = emptyList(),
    val isUserGenerated: Boolean = false,
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val compatibleGameModes: List<GameMode> = listOf(GameMode.CLASSIC),
    val depthLevel: Int? = null,
    val isDaily: Boolean = false,
    val isPremium: Boolean = false
)

/**
 * Question type (Truth or Dare)
 */
enum class QuestionType {
    TRUTH,
    DARE;
    
    companion object {
        fun fromString(value: String): QuestionType {
            return when (value.lowercase()) {
                "truth" -> TRUTH
                "dare" -> DARE
                else -> throw IllegalArgumentException("Invalid question type: $value")
            }
        }
    }
}

/**
 * Target type for questions (who the question is directed at)
 */
enum class TargetType {
    SINGLE, // Directed at a single player
    GROUP,  // Directed at the whole group
    PAIR,   // Directed at a pair of players
    SELF;   // Directed at the current player
    
    companion object {
        fun fromString(value: String): TargetType {
            return when (value.lowercase()) {
                "single" -> SINGLE
                "group" -> GROUP
                "pair" -> PAIR
                "self" -> SELF
                else -> throw IllegalArgumentException("Invalid target type: $value")
            }
        }
    }
}

/**
 * Question difficulty levels
 */
enum class QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXTREME;
    
    companion object {
        fun fromInt(value: Int): QuestionDifficulty {
            return when (value) {
                1 -> EASY
                2 -> MEDIUM
                3 -> HARD
                4, 5 -> EXTREME
                else -> MEDIUM
            }
        }
    }
}

/**
 * Extension function to convert legacy question format to enhanced question
 */
fun Map<String, Any>.toEnhancedQuestion(): EnhancedQuestion {
    val type = QuestionType.fromString(this["type"] as String)
    val text = this["text"] as String
    val category = this["category"] as String
    val targets = TargetType.fromString(this["targets"] as String)
    val depthLevel = this["depthLevel"] as? Int
    
    val tags = when (val tagsValue = this["tags"]) {
        is List<*> -> tagsValue.filterIsInstance<String>()
        else -> emptyList()
    }
    
    // Determine difficulty based on depth level or default to MEDIUM
    val difficulty = depthLevel?.let { QuestionDifficulty.fromInt(it) } ?: QuestionDifficulty.MEDIUM
    
    // Determine content filter level based on category and tags
    val contentFilterLevel = when {
        category == "family" || "family" in tags -> ContentFilterLevel.LOW
        category == "romantic" || "romantic" in tags || "intimate" in tags -> ContentFilterLevel.HIGH
        category == "party" && "drinking" in tags -> ContentFilterLevel.HIGH
        difficulty == QuestionDifficulty.EXTREME -> ContentFilterLevel.EXTREME
        else -> ContentFilterLevel.MEDIUM
    }
    
    // Determine compatible game modes based on category and tags
    val compatibleGameModes = mutableListOf(GameMode.CLASSIC)
    
    if (category == "family" || "family" in tags) {
        compatibleGameModes.add(GameMode.FAMILY)
    }
    
    if (category == "party" || "party" in tags) {
        compatibleGameModes.add(GameMode.PARTY)
    }
    
    if (category == "romantic" || "romantic" in tags || "intimate" in tags) {
        compatibleGameModes.add(GameMode.COUPLES)
    }
    
    if ("drinking" in tags || category == "party") {
        compatibleGameModes.add(GameMode.DRINK)
    }
    
    if (contentFilterLevel == ContentFilterLevel.MEDIUM) {
        compatibleGameModes.add(GameMode.TEEN)
    }
    
    if (contentFilterLevel == ContentFilterLevel.EXTREME) {
        compatibleGameModes.add(GameMode.HARDCORE)
    }
    
    return EnhancedQuestion(
        id = (this["id"] as? String) ?: UUID.randomUUID().toString(),
        type = type,
        text = text,
        category = category,
        targets = targets,
        difficulty = difficulty,
        contentFilterLevel = contentFilterLevel,
        tags = tags,
        depthLevel = depthLevel,
        compatibleGameModes = compatibleGameModes
    )
}