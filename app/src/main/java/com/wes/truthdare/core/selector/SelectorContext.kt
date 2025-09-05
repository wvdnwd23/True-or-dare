package com.wes.truthdare.core.selector

import com.wes.truthdare.core.agents.Mood

/**
 * Enum representing different game modes
 */
enum class GameMode {
    CASUAL,
    PARTY,
    DEEP_TALK,
    ROMANTIC,
    FAMILY_FRIENDLY
}

/**
 * Data class representing profile bias for question selection
 */
data class ProfileBias(
    val tagWeights: Map<String, Float>,
    val depthComfort: Int,
    val heatComfort: Int
)

/**
 * Data class representing the context for question selection
 */
data class SelectorContext(
    val playerId: String,
    val mode: GameMode,
    val heat: Int,              // 0..100
    val maxDepth: Int,          // Deep Talk cap
    val starTagsQueue: List<String>, // ⭐ geplande onderwerpen
    val lastTags: List<String>, // recent topics
    val bias: ProfileBias,
    val mood: Mood
)