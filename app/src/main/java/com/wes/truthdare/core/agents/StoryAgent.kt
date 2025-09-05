package com.wes.truthdare.core.agents

/**
 * Data class representing a summary of a game session
 */
data class StorySummary(
    val playerHighlights: Map<String, List<String>>, // Player ID -> List of highlights
    val moodJourney: List<Pair<String, Mood>>, // Player ID, Mood pairs in sequence
    val topTags: List<String>,
    val deepestMoment: String?, // Description of the deepest moment
    val funniestMoment: String? // Description of the funniest moment
)

/**
 * Interface for story generation and tracking functionality
 */
interface StoryAgent {
    /**
     * Record a question being asked and potentially answered
     * @param q The question that was asked
     * @param answer The answer given, or null if skipped
     */
    fun onAsked(q: Question, answer: String?)
    
    /**
     * Generate a chain of thematically related questions if appropriate
     * @return A list of 3-5 themed questions, or empty list if no chain is needed
     */
    fun chainIfNeeded(): List<Question>
    
    /**
     * Generate a summary of the current session
     * @return A StorySummary object containing session highlights
     */
    fun sessionSummary(): StorySummary
}