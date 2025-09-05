package com.wes.truthdare.ui.screens.reflect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.agents.StoryAgent
import com.wes.truthdare.core.agents.StorySummary
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.entities.GameSessionEntity
import com.wes.truthdare.core.data.entities.PlayerEntity
import com.wes.truthdare.core.data.repositories.GameSessionRepository
import com.wes.truthdare.core.data.repositories.JournalRepository
import com.wes.truthdare.core.data.repositories.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the reflect screen
 */
@HiltViewModel
class ReflectViewModel @Inject constructor(
    private val gameSessionRepository: GameSessionRepository,
    private val playerRepository: PlayerRepository,
    private val journalRepository: JournalRepository,
    private val appPreferences: AppPreferences,
    private val storyAgent: StoryAgent
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(ReflectUiState())
    val uiState: StateFlow<ReflectUiState> = _uiState.asStateFlow()
    
    /**
     * Load the session data
     * @param sessionId The session ID
     */
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            // Load session
            val session = gameSessionRepository.getSessionById(sessionId)
            if (session != null) {
                // Load players
                val players = playerRepository.getPlayersByIds(session.playerIds)
                
                // Get session summary
                val summary = storyAgent.sessionSummary()
                
                // Check if journal is enabled
                val journalEnabled = appPreferences.journalEnabled.value ?: false
                
                // Update UI state
                _uiState.value = _uiState.value.copy(
                    session = session,
                    players = players,
                    summary = summary,
                    journalEnabled = journalEnabled,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Save the session summary to the journal
     */
    fun saveToJournal() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val summary = _uiState.value.summary ?: return@launch
            
            // Create journal entry
            val title = "Sessie samenvatting - ${formatDate(session.startedAt)}"
            val content = generateJournalContent(summary, session)
            
            journalRepository.createEntry(
                sessionId = session.id,
                title = title,
                content = content,
                playerIds = session.playerIds,
                tags = summary.topTags,
                encrypt = true
            )
            
            _uiState.value = _uiState.value.copy(journalSaved = true)
        }
    }
    
    /**
     * Format a date as a string
     * @param timestamp The timestamp to format
     * @return The formatted date string
     */
    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Generate journal content from a summary
     * @param summary The session summary
     * @param session The game session
     * @return The generated journal content
     */
    private fun generateJournalContent(summary: StorySummary, session: GameSessionEntity): String {
        val sb = StringBuilder()
        
        // Add session info
        sb.appendLine("# Sessie Samenvatting")
        sb.appendLine()
        sb.appendLine("**Datum:** ${formatDate(session.startedAt)}")
        sb.appendLine("**Modus:** ${session.mode}")
        sb.appendLine("**Categorieën:** ${session.categories.joinToString(", ")}")
        sb.appendLine()
        
        // Add top tags
        sb.appendLine("## Populaire onderwerpen")
        summary.topTags.forEach { tag ->
            sb.appendLine("- $tag")
        }
        sb.appendLine()
        
        // Add player highlights
        sb.appendLine("## Speler Hoogtepunten")
        summary.playerHighlights.forEach { (playerId, highlights) ->
            val player = _uiState.value.players.find { it.id == playerId }
            if (player != null && highlights.isNotEmpty()) {
                sb.appendLine("### ${player.name}")
                highlights.forEach { highlight ->
                    sb.appendLine("- $highlight")
                }
                sb.appendLine()
            }
        }
        
        // Add special moments
        sb.appendLine("## Bijzondere momenten")
        summary.deepestMoment?.let {
            sb.appendLine("**Diepste moment:** $it")
            sb.appendLine()
        }
        summary.funniestMoment?.let {
            sb.appendLine("**Grappigste moment:** $it")
            sb.appendLine()
        }
        
        return sb.toString()
    }
}

/**
 * UI state for the reflect screen
 */
data class ReflectUiState(
    val session: GameSessionEntity? = null,
    val players: List<PlayerEntity> = emptyList(),
    val summary: StorySummary? = null,
    val journalEnabled: Boolean = false,
    val journalSaved: Boolean = false,
    val isLoading: Boolean = true
)