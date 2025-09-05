package com.wes.truthdare.core.impl

import com.wes.truthdare.core.agents.Question
import com.wes.truthdare.core.agents.SafetyAgent
import com.wes.truthdare.core.agents.SafetyDecision
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.nlp.TriggerScanner
import com.wes.truthdare.core.selector.SelectorContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of SafetyAgent
 */
@Singleton
class DefaultSafetyAgent @Inject constructor(
    private val nlpEngine: NlpEngine,
    private val triggerScanner: TriggerScanner
) : SafetyAgent {
    // List of tags that are considered sensitive at different heat levels
    private val sensitiveTagsByHeat = mapOf(
        0 to listOf("explicit", "alcohol", "drugs", "sexual", "controversial"),
        30 to listOf("explicit", "drugs", "sexual"),
        50 to listOf("explicit", "sexual"),
        70 to listOf("explicit"),
        90 to emptyList()
    )
    
    /**
     * Check if a question is appropriate for the current context
     * @param q The question to check
     * @param ctx The current selector context
     * @return A SafetyDecision indicating if the question is appropriate and providing an alternative if not
     */
    override fun check(q: Question, ctx: SelectorContext): SafetyDecision {
        // Check if the question contains sensitive tags for the current heat level
        val sensitiveThreshold = getSensitiveThresholdForHeat(ctx.heat)
        val hasSensitiveTags = q.tags.any { tag -> sensitiveTagsByHeat[sensitiveThreshold]?.contains(tag) ?: false }
        
        // Check if the question text contains trigger words
        val hasTriggersInText = runBlocking {
            triggerScanner.scanForTriggers(q.text)
        }
        
        // Check if the question's depth level exceeds the context's max depth
        val exceedsDepthLevel = q.depthLevel != null && q.depthLevel > ctx.maxDepth
        
        // If any checks fail, the question is not appropriate
        val isAppropriate = !hasSensitiveTags && !hasTriggersInText && !exceedsDepthLevel
        
        return if (isAppropriate) {
            SafetyDecision(ok = true, mildAlternative = null)
        } else {
            // Create a milder alternative question
            val mildAlternative = createMildAlternative(q, ctx)
            SafetyDecision(ok = false, mildAlternative = mildAlternative)
        }
    }
    
    /**
     * Check if an answer contains any trigger words or inappropriate content
     * @param answer The answer to check
     * @return True if the answer is appropriate, false otherwise
     */
    override fun checkAnswer(answer: String): Boolean {
        // Check if the answer contains trigger words
        val hasTriggersInText = runBlocking {
            triggerScanner.scanForTriggers(answer)
        }
        
        // Check if the answer has extremely negative sentiment
        val nlpResult = runBlocking {
            nlpEngine.analyze(answer)
        }
        val hasExtremeSentiment = nlpResult.sentiment < -70
        
        return !hasTriggersInText && !hasExtremeSentiment
    }
    
    /**
     * Get the sensitive threshold for a heat level
     * @param heat The heat level
     * @return The threshold key for sensitive tags
     */
    private fun getSensitiveThresholdForHeat(heat: Int): Int {
        return when {
            heat < 30 -> 0
            heat < 50 -> 30
            heat < 70 -> 50
            heat < 90 -> 70
            else -> 90
        }
    }
    
    /**
     * Create a milder alternative question
     * @param originalQuestion The original question
     * @param ctx The current selector context
     * @return A milder alternative question
     */
    private fun createMildAlternative(originalQuestion: Question, ctx: SelectorContext): Question {
        // Create a milder version based on the question type
        return if (originalQuestion.type == "truth") {
            Question(
                id = UUID.randomUUID().toString(),
                type = "truth",
                category = "casual",
                targets = originalQuestion.targets,
                depthLevel = minOf(ctx.maxDepth, originalQuestion.depthLevel ?: 1),
                tags = listOf("casual", "safe"),
                text = getMildTruthQuestion()
            )
        } else {
            Question(
                id = UUID.randomUUID().toString(),
                type = "dare",
                category = "casual",
                targets = originalQuestion.targets,
                depthLevel = null,
                tags = listOf("casual", "safe"),
                text = getMildDareQuestion()
            )
        }
    }
    
    /**
     * Get a mild truth question
     * @return A mild truth question
     */
    private fun getMildTruthQuestion(): String {
        val mildTruthQuestions = listOf(
            "Wat is je favoriete vakantiebestemming en waarom?",
            "Welk boek of film heeft de grootste indruk op je gemaakt?",
            "Als je één talent zou kunnen hebben, wat zou dat zijn?",
            "Wat is je favoriete maaltijd om te koken?",
            "Wat zou je doen als je een dag vrij hebt zonder verplichtingen?"
        )
        return mildTruthQuestions.random()
    }
    
    /**
     * Get a mild dare question
     * @return A mild dare question
     */
    private fun getMildDareQuestion(): String {
        val mildDareQuestions = listOf(
            "Doe een imitatie van je favoriete filmkarakter.",
            "Zing het refrein van je favoriete lied.",
            "Vertel een mop aan de groep.",
            "Doe een dansje van 10 seconden.",
            "Maak een compliment aan iedereen in de groep."
        )
        return mildDareQuestions.random()
    }
    
    /**
     * Helper function to run suspending functions in a blocking context
     * Only used for the check methods which are not suspending
     */
    private fun <T> runBlocking(block: suspend () -> T): T {
        return kotlinx.coroutines.runBlocking {
            block()
        }
    }
}