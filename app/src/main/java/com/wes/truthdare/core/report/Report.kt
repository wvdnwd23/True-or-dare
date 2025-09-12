package com.wes.truthdare.core.report

import com.wes.truthdare.core.data.GameMode
import java.time.Duration
import java.time.Instant

/**
 * Data model for a generated report
 */
data class Report(
    val summary: ReportSummary,
    val perPlayer: List<PlayerStats>,
    val rounds: List<RoundDetail>,
    val highlights: List<Highlight>
)

/**
 * Summary information for a report
 */
data class ReportSummary(
    val sessionId: String,
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration,
    val gameMode: GameMode,
    val drinkMode: Boolean,
    val playerCount: Int,
    val roundCount: Int,
    val truthCount: Int,
    val dareCount: Int,
    val skipCount: Int,
    val totalShots: Int
)

/**
 * Statistics for a player in a report
 */
data class PlayerStats(
    val playerId: String,
    val playerName: String,
    val truthsAnswered: Int,
    val daresCompleted: Int,
    val skipped: Int,
    val shotsTaken: Int,
    val completionRate: Float, // 0.0 - 1.0
    val mostCommonCategories: List<String>
)

/**
 * Detail for a round in a report
 */
data class RoundDetail(
    val roundNumber: Int,
    val playerName: String,
    val cardType: String,
    val question: String,
    val answer: String?,
    val completed: Boolean,
    val penalties: Int,
    val timestamp: Instant
)

/**
 * Highlight in a report
 */
data class Highlight(
    val type: HighlightType,
    val title: String,
    val description: String,
    val playerName: String?,
    val roundNumber: Int?
)

/**
 * Types of highlights
 */
enum class HighlightType {
    MOST_HONEST,
    DARE_DEVIL,
    MOST_SHOTS,
    MOST_SKIPS,
    FUNNIEST_ANSWER,
    HARDEST_DARE,
    LONGEST_ANSWER,
    QUICKEST_ANSWER
}