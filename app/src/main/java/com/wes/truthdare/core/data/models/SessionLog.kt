package com.wes.truthdare.core.data.models

import com.wes.truthdare.core.data.GameMode
import java.time.Instant

/**
 * Data model for a complete session log
 * Stores all information about a game session for report generation
 */
data class SessionLog(
    val sessionId: String,
    val startedAtUtc: Instant,
    val endedAtUtc: Instant?,
    val mode: GameMode,
    val drinkMode: Boolean,
    val locale: String,            // e.g. en-US
    val players: List<PlayerEntry>,
    val rounds: List<RoundEntry>,
    val totals: Totals
)

/**
 * Data model for a player in the session log
 */
data class PlayerEntry(
    val id: String, 
    val displayName: String
)

/**
 * Data model for a round in the session log
 */
data class RoundEntry(
    val roundNumber: Int,
    val playerId: String,
    val cardType: String,          // TRUTH/DARE
    val questionId: String?,       // or custom marker
    val questionText: String,      // localized snapshot
    val answerText: String?,       // user-provided answer (optional/nullable)
    val completed: Boolean,        // dare done?
    val penalties: Int,            // shots given in this round
    val timestampUtc: Instant
)

/**
 * Data model for session totals
 */
data class Totals(
    val truths: Int,
    val dares: Int,
    val skips: Int,
    val liarCaught: Int,
    val totalShots: Int,
    val perPlayerShots: Map<String, Int>,
    val perPlayerCompleted: Map<String, Int>
)