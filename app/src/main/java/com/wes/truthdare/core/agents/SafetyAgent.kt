package com.wes.truthdare.core.agents

import com.wes.truthdare.core.selector.SelectorContext

/**
 * Data class representing a safety decision about a question
 */
data class SafetyDecision(
    val ok: Boolean,
    val mildAlternative: Question?
)

/**
 * Interface for safety checking functionality
 */
interface SafetyAgent {
    /**
     * Check if a question is appropriate for the current context
     * @param q The question to check
     * @param ctx The current selector context
     * @return A SafetyDecision indicating if the question is appropriate and providing an alternative if not
     */
    fun check(q: Question, ctx: SelectorContext): SafetyDecision
    
    /**
     * Check if an answer contains any trigger words or inappropriate content
     * @param answer The answer to check
     * @return True if the answer is appropriate, false otherwise
     */
    fun checkAnswer(answer: String): Boolean
}