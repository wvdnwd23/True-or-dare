package com.wes.truthdare.core.agents

import com.wes.truthdare.core.selector.ProfileBias

/**
 * Enum representing different types of learning signals
 */
enum class SignalType {
    INTEREST,     // User showed interest in a topic (star button)
    DISCOMFORT,   // User showed discomfort with a question
    SKIP,         // User skipped a question
    ENGAGEMENT,   // User engaged deeply with a question
    LAUGHTER,     // User laughed during answer
    SILENCE       // User was silent or hesitant
}

/**
 * Data class representing a learning signal from user interaction
 */
data class LearningSignal(
    val playerId: String,
    val type: SignalType,
    val questionId: String?,
    val tags: List<String>,
    val heat: Int?,
    val depth: Int?
)

/**
 * Interface for learning and adapting to user preferences
 */
interface LearningAgent {
    /**
     * Update the learning model with new signals
     * @param sig The learning signal to process
     */
    fun updateSignals(sig: LearningSignal)
    
    /**
     * Get the current bias profile based on learning
     * @return The current ProfileBias
     */
    fun currentBias(): ProfileBias
}