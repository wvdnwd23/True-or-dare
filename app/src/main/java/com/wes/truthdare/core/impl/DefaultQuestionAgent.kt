package com.wes.truthdare.core.impl

import com.wes.truthdare.core.agents.Question
import com.wes.truthdare.core.agents.QuestionAgent
import com.wes.truthdare.core.data.repositories.QuestionRepository
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.selector.GameMode
import com.wes.truthdare.core.selector.SelectorContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Default implementation of QuestionAgent
 */
@Singleton
class DefaultQuestionAgent @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val nlpEngine: NlpEngine
) : QuestionAgent {
    /**
     * Get the next question based on the current context
     * @param ctx The current selector context
     * @return The selected question
     */
    override suspend fun nextQuestion(ctx: SelectorContext): Question = withContext(Dispatchers.Default) {
        // Get categories based on game mode
        val categories = getCategoriesForMode(ctx.mode)
        
        // Get questions of appropriate type and categories
        val questionType = if (Random.nextFloat() < 0.5f) "truth" else "dare"
        val questions = questionRepository.getQuestionsByTypeAndCategories(questionType, categories)
        
        // Filter questions based on context
        val filteredQuestions = filterQuestionsByContext(questions, ctx)
        
        // If no questions match the filters, return a random question
        if (filteredQuestions.isEmpty()) {
            return@withContext questions.randomOrNull() ?: createFallbackQuestion(questionType)
        }
        
        // Return a random question from the filtered list
        return@withContext filteredQuestions.random()
    }
    
    /**
     * Get a follow-up question based on an answer
     * @param answer The answer to generate a follow-up for
     * @param ctx The current selector context
     * @return A follow-up question, or null if no appropriate follow-up is available
     */
    override suspend fun followUpFor(answer: String, ctx: SelectorContext): Question? = withContext(Dispatchers.Default) {
        // Analyze the answer to extract tags and sentiment
        val nlpResult = nlpEngine.analyze(answer)
        
        // If no tags or intent were detected, we can't generate a relevant follow-up
        if (nlpResult.tags.isEmpty() && nlpResult.intent == null) {
            return@withContext null
        }
        
        // Get categories based on game mode
        val categories = getCategoriesForMode(ctx.mode)
        
        // Get questions that match the tags from the answer
        val tagQuestions = if (nlpResult.tags.isNotEmpty()) {
            questionRepository.getQuestionsByTags(nlpResult.tags, categories)
        } else {
            emptyList()
        }
        
        // Filter questions based on context
        val filteredQuestions = filterQuestionsByContext(tagQuestions, ctx)
        
        // If we have suitable questions, return one
        if (filteredQuestions.isNotEmpty()) {
            return@withContext filteredQuestions.random()
        }
        
        // If no suitable questions were found, create a generic follow-up
        return@withContext createGenericFollowUp(answer, ctx)
    }
    
    /**
     * Get categories for a game mode
     * @param mode The game mode
     * @return List of categories for the mode
     */
    private fun getCategoriesForMode(mode: GameMode): List<String> {
        return when (mode) {
            GameMode.CASUAL -> listOf("casual", "friends", "funny", "hypothetical")
            GameMode.PARTY -> listOf("party", "funny", "challenge", "hypothetical")
            GameMode.DEEP_TALK -> listOf("deep", "personal", "future", "hypothetical")
            GameMode.ROMANTIC -> listOf("romantic", "personal", "deep", "future")
            GameMode.FAMILY_FRIENDLY -> listOf("family", "casual", "childhood", "funny")
        }
    }
    
    /**
     * Filter questions based on context
     * @param questions The questions to filter
     * @param ctx The current selector context
     * @return Filtered list of questions
     */
    private fun filterQuestionsByContext(questions: List<Question>, ctx: SelectorContext): List<Question> {
        return questions.filter { question ->
            // Filter by depth level
            val depthOk = question.depthLevel == null || question.depthLevel <= ctx.maxDepth
            
            // Filter by heat level (assuming questions have implicit heat levels based on tags)
            val heatOk = !question.tags.contains("explicit") || ctx.heat >= 80
            
            // Apply bias - prefer questions with tags that have high weights in the bias
            val biasOk = question.tags.any { tag -> 
                ctx.bias.tagWeights[tag]?.let { weight -> weight > 0.7f } ?: true
            }
            
            depthOk && heatOk && biasOk
        }
    }
    
    /**
     * Create a fallback question when no suitable questions are found
     * @param type The question type ("truth" or "dare")
     * @return A fallback question
     */
    private fun createFallbackQuestion(type: String): Question {
        return if (type == "truth") {
            Question(
                id = UUID.randomUUID().toString(),
                type = "truth",
                category = "casual",
                targets = "single",
                depthLevel = 1,
                tags = listOf("casual", "simple"),
                text = "Wat is je favoriete film en waarom?"
            )
        } else {
            Question(
                id = UUID.randomUUID().toString(),
                type = "dare",
                category = "casual",
                targets = "single",
                depthLevel = null,
                tags = listOf("casual", "simple"),
                text = "Doe een imitatie van je favoriete filmkarakter."
            )
        }
    }
    
    /**
     * Create a generic follow-up question based on an answer
     * @param answer The answer to create a follow-up for
     * @param ctx The current selector context
     * @return A generic follow-up question
     */
    private fun createGenericFollowUp(answer: String, ctx: SelectorContext): Question {
        // Create a generic follow-up based on the length of the answer
        val isShortAnswer = answer.split(" ").size < 10
        
        return if (isShortAnswer) {
            Question(
                id = UUID.randomUUID().toString(),
                type = "truth",
                category = "casual",
                targets = "single",
                depthLevel = min(ctx.maxDepth, 2),
                tags = ctx.lastTags.take(2),
                text = "Kun je daar wat meer over vertellen?"
            )
        } else {
            Question(
                id = UUID.randomUUID().toString(),
                type = "truth",
                category = "casual",
                targets = "single",
                depthLevel = min(ctx.maxDepth, 2),
                tags = ctx.lastTags.take(2),
                text = "Hoe heeft dat je beïnvloed?"
            )
        }
    }
}