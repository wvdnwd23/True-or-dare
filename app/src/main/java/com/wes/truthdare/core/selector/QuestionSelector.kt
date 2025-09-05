package com.wes.truthdare.core.selector

import com.wes.truthdare.core.agents.Question
import com.wes.truthdare.core.agents.QuestionAgent
import com.wes.truthdare.core.agents.SafetyAgent
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class responsible for selecting questions based on context
 */
@Singleton
class QuestionSelector @Inject constructor(
    private val questionAgent: QuestionAgent,
    private val safetyAgent: SafetyAgent,
    private val nlpEngine: NlpEngine
) {
    // LRU cache to prevent recent question repetition
    private val recentQuestionsCache = LruCache<String, Question>(20)
    
    /**
     * Select the next question based on context
     * @param ctx The current selector context
     * @return The selected question
     */
    suspend fun selectNext(ctx: SelectorContext): Question = withContext(Dispatchers.Default) {
        // 1) Check star queue first - prioritize tag matches
        if (ctx.starTagsQueue.isNotEmpty()) {
            val starTag = ctx.starTagsQueue.first()
            matchByTag(starTag, ctx)?.let { 
                return@withContext it 
            }
        }
        
        // 2) Apply bias, heat, depth filters and anti-repeat
        val pool = poolByModeAndFilters(ctx)
            .biasedBy(ctx.bias, ctx.heat)
            .antiRepeat()
        
        // 3) Apply safety checks
        val safe = pool.firstOrNull { q -> safetyAgent.check(q, ctx).ok }
            ?: safetyAgent.check(pool.first(), ctx).mildAlternative
            ?: fallbackMild(ctx)
        
        return@withContext safe
    }
    
    /**
     * Check if a follow-up question should be asked based on an answer
     * @param answer The answer to analyze
     * @param ctx The current selector context
     * @return A follow-up question, or null if no follow-up is appropriate
     */
    suspend fun maybeAskFollowUp(answer: String, ctx: SelectorContext): Question? = withContext(Dispatchers.Default) {
        val nlp = nlpEngine.analyze(answer)
        
        if (!nlp.triggered) {
            // Max 1 follow-up per answer, high relevance required
            if (nlp.intent != null || nlp.tags.intersect(ctx.lastTags.toSet()).isNotEmpty()) {
                val followUp = questionAgent.followUpFor(answer, ctx) ?: return@withContext null
                val safetyDecision = safetyAgent.check(followUp, ctx)
                
                return@withContext if (safetyDecision.ok) {
                    followUp
                } else {
                    safetyDecision.mildAlternative
                }
            }
        } else {
            // Triggered content detected, suggest a break
            suggestSmartBreak()
        }
        
        return@withContext null
    }
    
    /**
     * Find a question matching a specific tag
     * @param tag The tag to match
     * @param ctx The current selector context
     * @return A matching question, or null if no match is found
     */
    private suspend fun matchByTag(tag: String, ctx: SelectorContext): Question? {
        val candidates = questionAgent.nextQuestion(
            ctx.copy(
                lastTags = listOf(tag) + ctx.lastTags
            )
        )
        
        // Check if the question is safe
        val safetyDecision = safetyAgent.check(candidates, ctx)
        return if (safetyDecision.ok) {
            candidates
        } else {
            safetyDecision.mildAlternative
        }
    }
    
    /**
     * Get a pool of questions filtered by mode and context
     * @param ctx The current selector context
     * @return A list of filtered questions
     */
    private suspend fun poolByModeAndFilters(ctx: SelectorContext): List<Question> {
        // In a real implementation, this would query the database or repository
        // For now, we'll just get a single question from the agent
        return listOf(questionAgent.nextQuestion(ctx))
    }
    
    /**
     * Apply bias and heat filters to a question pool
     * @param bias The profile bias to apply
     * @param heat The heat level to apply
     * @return A filtered list of questions
     */
    private fun List<Question>.biasedBy(bias: ProfileBias, heat: Int): List<Question> {
        // In a real implementation, this would sort and filter the questions
        // based on the bias and heat level
        return this
    }
    
    /**
     * Filter out recently asked questions
     * @return A list of questions without recent repeats
     */
    private fun List<Question>.antiRepeat(): List<Question> {
        return this.filter { q -> recentQuestionsCache.get(q.id) == null }
    }
    
    /**
     * Get a mild fallback question when no safe questions are available
     * @param ctx The current selector context
     * @return A mild, safe question
     */
    private suspend fun fallbackMild(ctx: SelectorContext): Question {
        // In a real implementation, this would return a predefined safe question
        // For now, we'll just get a question from the agent
        return questionAgent.nextQuestion(
            ctx.copy(
                heat = 0,
                maxDepth = 1
            )
        )
    }
    
    /**
     * Suggest a smart break when triggered content is detected
     * @return A question suggesting a break or mini-game
     */
    private fun suggestSmartBreak(): Question? {
        // In a real implementation, this would return a question suggesting a break
        return null
    }
    
    /**
     * Add a question to the recent questions cache
     * @param question The question to add
     */
    fun addToRecentQuestions(question: Question) {
        recentQuestionsCache.put(question.id, question)
    }
}