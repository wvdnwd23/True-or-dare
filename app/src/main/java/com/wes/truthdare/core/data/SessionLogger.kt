package com.wes.truthdare.core.data

import com.wes.truthdare.core.data.models.PlayerEntry
import com.wes.truthdare.core.data.models.RoundEntry
import com.wes.truthdare.core.data.models.SessionLog
import com.wes.truthdare.core.data.models.Totals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logger for game sessions
 * Tracks all events during a game session for report generation
 */
@Singleton
class SessionLogger @Inject constructor() {
    
    // Current session log
    private val _sessionLog = MutableStateFlow<SessionLog?>(null)
    val sessionLog: StateFlow<SessionLog?> = _sessionLog.asStateFlow()
    
    // Current round number
    private var currentRoundNumber = 0
    
    // Mutable collections for updating
    private val mutablePlayers = mutableListOf<PlayerEntry>()
    private val mutableRounds = mutableListOf<RoundEntry>()
    private var mutableTotals = Totals(
        truths = 0,
        dares = 0,
        skips = 0,
        liarCaught = 0,
        totalShots = 0,
        perPlayerShots = mutableMapOf(),
        perPlayerCompleted = mutableMapOf()
    )
    
    /**
     * Start a new session
     * @param mode The game mode
     * @param drinkMode Whether drink mode is enabled
     * @return The session ID
     */
    fun startSession(mode: GameMode, drinkMode: Boolean): String {
        val sessionId = UUID.randomUUID().toString()
        
        // Reset state
        mutablePlayers.clear()
        mutableRounds.clear()
        currentRoundNumber = 0
        mutableTotals = Totals(
            truths = 0,
            dares = 0,
            skips = 0,
            liarCaught = 0,
            totalShots = 0,
            perPlayerShots = mutableMapOf(),
            perPlayerCompleted = mutableMapOf()
        )
        
        // Create new session log
        val newSessionLog = SessionLog(
            sessionId = sessionId,
            startedAtUtc = Instant.now(),
            endedAtUtc = null,
            mode = mode,
            drinkMode = drinkMode,
            locale = Locale.getDefault().toLanguageTag(),
            players = mutablePlayers,
            rounds = mutableRounds,
            totals = mutableTotals
        )
        
        _sessionLog.value = newSessionLog
        return sessionId
    }
    
    /**
     * Add a player to the session
     * @param id The player ID
     * @param name The player name
     */
    fun addPlayer(id: String, name: String) {
        val player = PlayerEntry(id = id, displayName = name)
        mutablePlayers.add(player)
        
        // Initialize player stats in totals
        (mutableTotals.perPlayerShots as MutableMap)[id] = 0
        (mutableTotals.perPlayerCompleted as MutableMap)[id] = 0
        
        // Update session log
        updateSessionLog()
    }
    
    /**
     * Record a round in the session
     * @param playerId The ID of the player who played the round
     * @param cardType The type of card (TRUTH/DARE)
     * @param questionId The ID of the question
     * @param questionText The text of the question
     * @param answerText The player's answer (if any)
     * @param completed Whether the dare was completed
     * @param penalties The number of penalties (shots) given in this round
     */
    fun recordRound(
        playerId: String,
        cardType: String,
        questionId: String?,
        questionText: String,
        answerText: String?,
        completed: Boolean,
        penalties: Int
    ) {
        // Increment round number
        currentRoundNumber++
        
        // Create round entry
        val roundEntry = RoundEntry(
            roundNumber = currentRoundNumber,
            playerId = playerId,
            cardType = cardType,
            questionId = questionId,
            questionText = questionText,
            answerText = answerText,
            completed = completed,
            penalties = penalties,
            timestampUtc = Instant.now()
        )
        
        // Add to rounds list
        mutableRounds.add(roundEntry)
        
        // Update totals
        when (cardType) {
            "TRUTH" -> mutableTotals = mutableTotals.copy(truths = mutableTotals.truths + 1)
            "DARE" -> mutableTotals = mutableTotals.copy(dares = mutableTotals.dares + 1)
        }
        
        if (!completed) {
            mutableTotals = mutableTotals.copy(skips = mutableTotals.skips + 1)
        } else {
            // Increment completed count for player
            val playerCompleted = mutableTotals.perPlayerCompleted[playerId] ?: 0
            (mutableTotals.perPlayerCompleted as MutableMap)[playerId] = playerCompleted + 1
        }
        
        if (penalties > 0) {
            // Update total shots
            mutableTotals = mutableTotals.copy(totalShots = mutableTotals.totalShots + penalties)
            
            // Update player shots
            val playerShots = mutableTotals.perPlayerShots[playerId] ?: 0
            (mutableTotals.perPlayerShots as MutableMap)[playerId] = playerShots + penalties
        }
        
        // Update session log
        updateSessionLog()
    }
    
    /**
     * Record a liar caught event
     * @param playerId The ID of the player who was caught lying
     * @param penalties The number of penalties (shots) given
     */
    fun recordLiarCaught(playerId: String, penalties: Int) {
        // Update totals
        mutableTotals = mutableTotals.copy(liarCaught = mutableTotals.liarCaught + 1)
        
        if (penalties > 0) {
            // Update total shots
            mutableTotals = mutableTotals.copy(totalShots = mutableTotals.totalShots + penalties)
            
            // Update player shots
            val playerShots = mutableTotals.perPlayerShots[playerId] ?: 0
            (mutableTotals.perPlayerShots as MutableMap)[playerId] = playerShots + penalties
        }
        
        // Update session log
        updateSessionLog()
    }
    
    /**
     * End the current session
     */
    fun endSession() {
        val currentLog = _sessionLog.value ?: return
        
        // Update end time
        _sessionLog.value = currentLog.copy(endedAtUtc = Instant.now())
    }
    
    /**
     * Get a snapshot of the current session log
     * @return The current session log, or null if no session is active
     */
    fun getSessionLogSnapshot(): SessionLog? {
        return _sessionLog.value
    }
    
    /**
     * Clear the current session log
     */
    fun clearSessionLog() {
        _sessionLog.value = null
    }
    
    /**
     * Update the session log with the latest data
     */
    private fun updateSessionLog() {
        val currentLog = _sessionLog.value ?: return
        
        _sessionLog.value = currentLog.copy(
            players = mutablePlayers.toList(),
            rounds = mutableRounds.toList(),
            totals = mutableTotals
        )
    }
}