package com.wes.truthdare.core.impl

import com.wes.truthdare.core.agents.EmotionAgent
import com.wes.truthdare.core.agents.EmotionTag
import com.wes.truthdare.core.agents.Mood
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.util.TranscriptEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of EmotionAgent
 */
@Singleton
class DefaultEmotionAgent @Inject constructor(
    private val nlpEngine: NlpEngine
) : EmotionAgent {
    /**
     * Analyze the emotional content of a transcript
     * @param e The transcript event to analyze
     * @return EmotionTag containing mood and stress level
     */
    override fun analyze(e: TranscriptEvent): EmotionTag {
        // Calculate stress level based on prosody and sentiment
        val stressLevel = calculateStressLevel(e)
        
        // Determine mood based on sentiment, prosody, and content
        val mood = determineMood(e, stressLevel)
        
        return EmotionTag(mood, stressLevel)
    }
    
    /**
     * Calculate stress level based on prosody and transcript
     * @param e The transcript event
     * @return Stress level (0-100)
     */
    private fun calculateStressLevel(e: TranscriptEvent): Int {
        // Higher speech rate indicates more stress
        val rateStress = (e.prosody.rate * 20).coerceIn(0f, 40f).toInt()
        
        // More silences can indicate hesitation and stress
        val silenceCount = e.prosody.silences.size
        val silenceStress = (silenceCount * 5).coerceIn(0, 30)
        
        // Pitch variations can indicate stress
        val pitchStress = if (e.prosody.pitch > 0.7f) {
            ((e.prosody.pitch - 0.7f) * 100).coerceIn(0f, 30f).toInt()
        } else {
            0
        }
        
        // Combine factors
        return (rateStress + silenceStress + pitchStress).coerceIn(0, 100)
    }
    
    /**
     * Determine mood based on transcript and stress level
     * @param e The transcript event
     * @param stressLevel The calculated stress level
     * @return The determined mood
     */
    private fun determineMood(e: TranscriptEvent, stressLevel: Int): Mood {
        // Run NLP analysis on the transcript text
        val nlpResult = runBlocking {
            nlpEngine.analyze(e.text)
        }
        
        // Determine mood based on sentiment and stress
        return when {
            nlpResult.sentiment > 50 && stressLevel < 30 -> Mood.HAPPY
            nlpResult.sentiment > 0 && stressLevel < 50 -> Mood.CALM
            stressLevel > 70 -> Mood.NERVOUS
            else -> Mood.SERIOUS
        }
    }
    
    /**
     * Helper function to run suspending functions in a blocking context
     * Only used for the analyze method which is not suspending
     */
    private fun <T> runBlocking(block: suspend () -> T): T {
        return kotlinx.coroutines.runBlocking {
            block()
        }
    }
}