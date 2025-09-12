package com.wes.truthdare.core.report

import com.wes.truthdare.core.data.models.SessionLog
import java.time.Duration
import javax.inject.Inject

/**
 * Interface for building reports from session logs
 */
interface ReportBuilder {
    /**
     * Build a report from a session log
     * @param log The session log
     * @param redactAnswers Whether to redact answers in the report
     * @param anonymize Whether to anonymize player names in the report
     * @return The generated report
     */
    fun build(log: SessionLog, redactAnswers: Boolean = false, anonymize: Boolean = false): Report
}

/**
 * Default implementation of ReportBuilder
 */
class DefaultReportBuilder @Inject constructor() : ReportBuilder {
    
    override fun build(log: SessionLog, redactAnswers: Boolean, anonymize: Boolean): Report {
        // Create summary
        val duration = if (log.endedAtUtc != null) {
            Duration.between(log.startedAtUtc, log.endedAtUtc)
        } else {
            Duration.between(log.startedAtUtc, log.startedAtUtc)
        }
        
        val summary = ReportSummary(
            sessionId = log.sessionId,
            startTime = log.startedAtUtc,
            endTime = log.endedAtUtc ?: log.startedAtUtc,
            duration = duration,
            gameMode = log.mode,
            drinkMode = log.drinkMode,
            playerCount = log.players.size,
            roundCount = log.rounds.size,
            truthCount = log.totals.truths,
            dareCount = log.totals.dares,
            skipCount = log.totals.skips,
            totalShots = log.totals.totalShots
        )
        
        // Create per-player stats
        val perPlayer = log.players.map { player ->
            val playerId = player.id
            val playerName = if (anonymize) "Player ${log.players.indexOf(player) + 1}" else player.displayName
            
            // Count truths and dares for this player
            val playerRounds = log.rounds.filter { it.playerId == playerId }
            val truthsAnswered = playerRounds.count { it.cardType == "TRUTH" && it.completed }
            val daresCompleted = playerRounds.count { it.cardType == "DARE" && it.completed }
            val skipped = playerRounds.count { !it.completed }
            val shotsTaken = log.totals.perPlayerShots[playerId] ?: 0
            
            // Calculate completion rate
            val completionRate = if (playerRounds.isNotEmpty()) {
                playerRounds.count { it.completed }.toFloat() / playerRounds.size
            } else {
                0f
            }
            
            // Find most common categories (would need to extract from question tags)
            val mostCommonCategories = listOf("casual", "party") // Placeholder
            
            PlayerStats(
                playerId = playerId,
                playerName = playerName,
                truthsAnswered = truthsAnswered,
                daresCompleted = daresCompleted,
                skipped = skipped,
                shotsTaken = shotsTaken,
                completionRate = completionRate,
                mostCommonCategories = mostCommonCategories
            )
        }
        
        // Create round details
        val rounds = log.rounds.map { round ->
            val player = log.players.find { it.id == round.playerId }
            val playerName = if (anonymize) {
                "Player ${log.players.indexOf(player) + 1}"
            } else {
                player?.displayName ?: "Unknown"
            }
            
            val answer = if (redactAnswers) {
                null
            } else {
                round.answerText
            }
            
            RoundDetail(
                roundNumber = round.roundNumber,
                playerName = playerName,
                cardType = round.cardType,
                question = round.questionText,
                answer = answer,
                completed = round.completed,
                penalties = round.penalties,
                timestamp = round.timestampUtc
            )
        }
        
        // Generate highlights
        val highlights = generateHighlights(log, perPlayer, rounds, anonymize)
        
        return Report(
            summary = summary,
            perPlayer = perPlayer,
            rounds = rounds,
            highlights = highlights
        )
    }
    
    /**
     * Generate highlights from the session data
     */
    private fun generateHighlights(
        log: SessionLog,
        playerStats: List<PlayerStats>,
        rounds: List<RoundDetail>,
        anonymize: Boolean
    ): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        
        // Most honest player (highest completion rate)
        playerStats.maxByOrNull { it.completionRate }?.let { player ->
            if (player.completionRate > 0.8f) { // Only if completion rate is high enough
                highlights.add(
                    Highlight(
                        type = HighlightType.MOST_HONEST,
                        title = "Most Honest Player",
                        description = "${player.playerName} answered ${(player.completionRate * 100).toInt()}% of questions!",
                        playerName = player.playerName,
                        roundNumber = null
                    )
                )
            }
        }
        
        // Dare devil (most dares completed)
        playerStats.maxByOrNull { it.daresCompleted }?.let { player ->
            if (player.daresCompleted > 0) {
                highlights.add(
                    Highlight(
                        type = HighlightType.DARE_DEVIL,
                        title = "Dare Devil",
                        description = "${player.playerName} completed ${player.daresCompleted} dares!",
                        playerName = player.playerName,
                        roundNumber = null
                    )
                )
            }
        }
        
        // Most shots
        playerStats.maxByOrNull { it.shotsTaken }?.let { player ->
            if (player.shotsTaken > 0) {
                highlights.add(
                    Highlight(
                        type = HighlightType.MOST_SHOTS,
                        title = "Shot Champion",
                        description = "${player.playerName} took ${player.shotsTaken} shots!",
                        playerName = player.playerName,
                        roundNumber = null
                    )
                )
            }
        }
        
        // Most skips
        playerStats.maxByOrNull { it.skipped }?.let { player ->
            if (player.skipped > 0) {
                highlights.add(
                    Highlight(
                        type = HighlightType.MOST_SKIPS,
                        title = "Most Secretive",
                        description = "${player.playerName} skipped ${player.skipped} questions!",
                        playerName = player.playerName,
                        roundNumber = null
                    )
                )
            }
        }
        
        // Hardest dare (if we had difficulty ratings)
        // For now, just pick a random completed dare
        rounds.filter { it.cardType == "DARE" && it.completed }.randomOrNull()?.let { round ->
            highlights.add(
                Highlight(
                    type = HighlightType.HARDEST_DARE,
                    title = "Impressive Dare",
                    description = "${round.playerName} completed: &quot;${round.question}&quot;",
                    playerName = round.playerName,
                    roundNumber = round.roundNumber
                )
            )
        }
        
        return highlights
    }
}