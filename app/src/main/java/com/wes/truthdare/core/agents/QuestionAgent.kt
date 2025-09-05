package com.wes.truthdare.core.agents

import com.wes.truthdare.core.selector.SelectorContext

/**
 * Data class representing a question in the game
 */
data class Question(
    val id: String,
    val type: String, // "truth" or "dare"
    val category: String,
    val targets: String, // "single", "group", etc.
    val depthLevel: Int?, // null for non-deep talk questions
    val tags: List<String>,
    val text: String
)

/**
 * Interface for question selection functionality
 */
interface QuestionAgent {
    /**
     * Get the next question based on the current context
     * @param ctx The current selector context
     * @return The selected question
     */
    suspend fun nextQuestion(ctx: SelectorContext): Question
    
    /**
     * Get a follow-up question based on an answer
     * @param answer The answer to generate a follow-up for
     * @param ctx The current selector context
     * @return A follow-up question, or null if no appropriate follow-up is available
     */
    suspend fun followUpFor(answer: String, ctx: SelectorContext): Question?
}