package com.wes.truthdare.core.impl

import com.wes.truthdare.core.agents.Mood
import com.wes.truthdare.core.agents.Question
import com.wes.truthdare.core.agents.StoryAgent
import com.wes.truthdare.core.agents.StorySummary
import com.wes.truthdare.core.data.repositories.QuestionHistoryRepository
import com.wes.truthdare.core.nlp.NlpEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of StoryAgent
 */
@Singleton
class DefaultStoryAgent @Inject constructor(
    private val questionHistoryRepository: QuestionHistoryRepository,
    private val nlpEngine: NlpEngine
) : StoryAgent {
    // Store questions and answers for the current session
    private val sessionQuestions = mutableListOf<QuestionRecord>()
    
    // Track mood changes throughout the session
    private val moodJourney = mutableListOf<Pair<String, Mood>>()
    
    // Track potential themes for question chains
    private val themeTracker = mutableMapOf<String, Int>()
    
    // Current chain of questions (if active)
    private var activeChain: List<Question>? = null
    
    /**
     * Record a question being asked and potentially answered
     * @param q The question that was asked
     * @param answer The answer given, or null if skipped
     */
    override fun onAsked(q: Question, answer: String?) {
        // Record the question and answer
        val record = QuestionRecord(
            question = q,
            answer = answer,
            timestamp = System.currentTimeMillis(),
            playerId = "", // This would be set from the actual context
            sentiment = if (answer != null) {
                runBlocking { nlpEngine.analyze(answer).sentiment }
            } else {
                0
            }
        )
        
        sessionQuestions.add(record)
        
        // Update theme tracker with tags from this question
        if (answer != null) {
            q.tags.forEach { tag ->
                themeTracker[tag] = (themeTracker[tag] ?: 0) + 1
            }
        }
        
        // If we have an active chain and this question was from it, remove it from the chain
        activeChain?.let { chain ->
            if (chain.any { it.id == q.id }) {
                activeChain = chain.filter { it.id != q.id }
                if (activeChain?.isEmpty() == true) {
                    activeChain = null
                }
            }
        }
    }
    
    /**
     * Generate a chain of thematically related questions if appropriate
     * @return A list of 3-5 themed questions, or empty list if no chain is needed
     */
    override fun chainIfNeeded(): List<Question> {
        // If we already have an active chain, return it
        activeChain?.let {
            return it
        }
        
        // Only create a chain if we have enough questions in the session
        if (sessionQuestions.size < 5) {
            return emptyList()
        }
        
        // Find the most common theme in recent questions
        val dominantTheme = themeTracker.entries
            .sortedByDescending { it.value }
            .firstOrNull()?.key
            
        // Only create a chain if we have a dominant theme and it's not too common
        if (dominantTheme == null || themeTracker[dominantTheme]!! < 3) {
            return emptyList()
        }
        
        // Create a chain of 3-5 questions based on the dominant theme
        val chainLength = (3..5).random()
        val chain = (1..chainLength).map { i ->
            createThemedQuestion(dominantTheme, i)
        }
        
        // Store the chain for future reference
        activeChain = chain
        
        return chain
    }
    
    /**
     * Generate a summary of the current session
     * @return A StorySummary object containing session highlights
     */
    override fun sessionSummary(): StorySummary {
        // Group questions by player
        val questionsByPlayer = sessionQuestions.groupBy { it.playerId }
        
        // Create highlights for each player
        val playerHighlights = questionsByPlayer.mapValues { (_, questions) ->
            createPlayerHighlights(questions)
        }
        
        // Find the top tags from the session
        val topTags = themeTracker.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
        
        // Find the deepest moment
        val deepestMoment = findDeepestMoment()
        
        // Find the funniest moment
        val funniestMoment = findFunniestMoment()
        
        return StorySummary(
            playerHighlights = playerHighlights,
            moodJourney = moodJourney.toList(),
            topTags = topTags,
            deepestMoment = deepestMoment,
            funniestMoment = funniestMoment
        )
    }
    
    /**
     * Create a themed question
     * @param theme The theme for the question
     * @param sequenceNumber The position in the chain
     * @return A themed question
     */
    private fun createThemedQuestion(theme: String, sequenceNumber: Int): Question {
        // In a real implementation, this would use the QuestionRepository to find
        // questions with the given theme. For now, we'll create a placeholder.
        
        val questionText = when (sequenceNumber) {
            1 -> "Wat vind je het meest interessant aan $theme?"
            2 -> "Heb je een bijzondere ervaring gehad met $theme?"
            3 -> "Hoe heeft $theme je leven beïnvloed?"
            4 -> "Wat zou je willen veranderen aan hoe mensen omgaan met $theme?"
            else -> "Wat denk je dat de toekomst brengt voor $theme?"
        }
        
        return Question(
            id = UUID.randomUUID().toString(),
            type = "truth",
            category = "deep",
            targets = "single",
            depthLevel = sequenceNumber.coerceAtMost(5),
            tags = listOf(theme, "chain", "themed"),
            text = questionText
        )
    }
    
    /**
     * Create highlights for a player
     * @param questions The player's questions and answers
     * @return List of highlight descriptions
     */
    private fun createPlayerHighlights(questions: List<QuestionRecord>): List<String> {
        val highlights = mutableListOf<String>()
        
        // Find questions with high sentiment (positive or negative)
        questions.filter { it.answer != null && Math.abs(it.sentiment) > 50 }
            .take(2)
            .forEach { record ->
                val sentiment = if (record.sentiment > 0) "enthousiast" else "serieus"
                highlights.add("Reageerde $sentiment op: &quot;${record.question.text}&quot;")
            }
        
        // Find skipped questions
        questions.filter { it.answer == null }
            .take(1)
            .forEach { record ->
                highlights.add("Sloeg de vraag over: &quot;${record.question.text}&quot;")
            }
        
        // Find deep questions
        questions.filter { it.question.depthLevel != null && it.question.depthLevel >= 3 && it.answer != null }
            .take(1)
            .forEach { record ->
                highlights.add("Deelde een diep inzicht bij: &quot;${record.question.text}&quot;")
            }
        
        return highlights
    }
    
    /**
     * Find the deepest moment in the session
     * @return Description of the deepest moment, or null if none
     */
    private fun findDeepestMoment(): String? {
        // Find the question with the highest depth level that was answered
        return sessionQuestions
            .filter { it.answer != null && it.question.depthLevel != null }
            .maxByOrNull { it.question.depthLevel!! }
            ?.let { record ->
                "&quot;${record.question.text}&quot; leidde tot een diep moment van reflectie."
            }
    }
    
    /**
     * Find the funniest moment in the session
     * @return Description of the funniest moment, or null if none
     */
    private fun findFunniestMoment(): String? {
        // Find a dare question that was likely funny
        return sessionQuestions
            .filter { it.answer != null && it.question.type == "dare" }
            .randomOrNull()
            ?.let { record ->
                "Er was veel gelach bij de opdracht: &quot;${record.question.text}&quot;"
            }
    }
    
    /**
     * Helper function to run suspending functions in a blocking context
     */
    private fun <T> runBlocking(block: suspend () -> T): T {
        return kotlinx.coroutines.runBlocking {
            block()
        }
    }
    
    /**
     * Data class to store a question record
     */
    private data class QuestionRecord(
        val question: Question,
        val answer: String?,
        val timestamp: Long,
        val playerId: String,
        val sentiment: Int
    )
}