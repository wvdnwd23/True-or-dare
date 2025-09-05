package com.wes.truthdare.core.util

/**
 * Data class representing voice transcript with metadata
 */
data class TranscriptEvent(
    val text: String,
    val confidence: Float,
    val prosody: Prosody
)

/**
 * Data class representing voice prosody characteristics
 */
data class Prosody(
    val pitch: Float, // Normalized pitch value (0.0-1.0)
    val rate: Float,  // Speech rate (words per second)
    val silences: List<SilencePeriod> // Silence periods in the speech
)

/**
 * Data class representing a period of silence in speech
 */
data class SilencePeriod(
    val startMs: Long, // Start time in milliseconds
    val durationMs: Long // Duration in milliseconds
)