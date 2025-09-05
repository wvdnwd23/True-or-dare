package com.wes.truthdare.core.agents

import com.wes.truthdare.core.util.TranscriptEvent

/**
 * Enum representing different mood states
 */
enum class Mood { 
    HAPPY, 
    CALM, 
    SERIOUS, 
    NERVOUS 
}

/**
 * Data class representing emotional state with mood and stress level
 */
data class EmotionTag(
    val mood: Mood, 
    val stress: Int // 0..100
)

/**
 * Interface for emotion analysis functionality
 */
interface EmotionAgent {
    /**
     * Analyze the emotional content of a transcript
     * @param e The transcript event to analyze
     * @return EmotionTag containing mood and stress level
     */
    fun analyze(e: TranscriptEvent): EmotionTag
}