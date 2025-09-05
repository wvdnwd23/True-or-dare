package com.wes.truthdare.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.agents.EmotionAgent
import com.wes.truthdare.core.agents.LearningAgent
import com.wes.truthdare.core.agents.LearningSignal
import com.wes.truthdare.core.agents.Mood
import com.wes.truthdare.core.agents.Question
import com.wes.truthdare.core.agents.QuestionAgent
import com.wes.truthdare.core.agents.SafetyAgent
import com.wes.truthdare.core.agents.SignalType
import com.wes.truthdare.core.agents.StoryAgent
import com.wes.truthdare.core.agents.VoiceAgent
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.entities.GameSessionEntity
import com.wes.truthdare.core.data.entities.PlayerEntity
import com.wes.truthdare.core.data.repositories.GameSessionRepository
import com.wes.truthdare.core.data.repositories.PlayerPreferenceRepository
import com.wes.truthdare.core.data.repositories.PlayerRepository
import com.wes.truthdare.core.data.repositories.QuestionHistoryRepository
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.selector.ProfileBias
import com.wes.truthdare.core.selector.QuestionSelector
import com.wes.truthdare.core.selector.SelectorContext
import com.wes.truthdare.core.util.TranscriptEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

/**
 * ViewModel for the game screen
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameSessionRepository: GameSessionRepository,
    private val playerRepository: PlayerRepository,
    private val questionHistoryRepository: QuestionHistoryRepository,
    private val playerPreferenceRepository: PlayerPreferenceRepository,
    private val appPreferences: AppPreferences,
    private val questionAgent: QuestionAgent,
    private val safetyAgent: SafetyAgent,
    private val emotionAgent: EmotionAgent,
    private val storyAgent: StoryAgent,
    private val learningAgent: LearningAgent,
    private val voiceAgent: VoiceAgent,
    private val nlpEngine: NlpEngine,
    private val questionSelector: QuestionSelector
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    // Current game session
    private var gameSession: GameSessionEntity? = null
    
    // Players in the game
    private var players: List<PlayerEntity> = emptyList()
    
    // Current player index
    private var currentPlayerIndex = 0
    
    // Star tags queue for each player
    private val starTagsQueues: MutableMap<String, Queue<String>> = mutableMapOf()
    
    // Voice listening job
    private var voiceListeningJob: Job? = null
    
    // Timer job
    private var timerJob: Job? = null
    
    /**
     * Load the game session
     * @param sessionId The session ID
     */
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            // Load session
            val session = gameSessionRepository.getSessionById(sessionId)
            if (session != null) {
                gameSession = session
                
                // Load players
                players = playerRepository.getPlayersByIds(session.playerIds)
                
                // Initialize star tags queues
                players.forEach { player ->
                    starTagsQueues[player.id] = LinkedList()
                }
                
                // Load voice mode setting
                val voiceModeEnabled = appPreferences.voiceModeEnabled.collectLatest { enabled ->
                    _uiState.value = _uiState.value.copy(voiceModeEnabled = enabled)
                }
                
                // Set initial player
                setCurrentPlayer(0)
                
                // Load first question
                loadNextQuestion()
            }
        }
    }
    
    /**
     * Load the next question
     */
    fun loadNextQuestion() {
        viewModelScope.launch {
            val session = gameSession ?: return@launch
            val currentPlayer = getCurrentPlayer() ?: return@launch
            
            // Get player bias
            val playerBias = playerPreferenceRepository.getProfileBiasForPlayerSync(currentPlayer.id)
            
            // Get star tags for the player
            val starTags = starTagsQueues[currentPlayer.id]?.toList() ?: emptyList()
            
            // Create selector context
            val context = SelectorContext(
                playerId = currentPlayer.id,
                mode = session.mode,
                heat = session.maxHeat,
                maxDepth = session.maxDepth,
                starTagsQueue = starTags,
                lastTags = _uiState.value.currentQuestion?.tags ?: emptyList(),
                bias = playerBias,
                mood = _uiState.value.currentMood
            )
            
            // Select next question
            val question = questionSelector.selectNext(context)
            
            // Record question in history
            val questionHistory = questionHistoryRepository.recordQuestion(
                sessionId = session.id,
                playerId = currentPlayer.id,
                question = question,
                wasSkipped = false
            )
            
            // Update story agent
            storyAgent.onAsked(question, null)
            
            // Update UI state
            _uiState.value = _uiState.value.copy(
                currentQuestion = question,
                currentQuestionId = questionHistory.id,
                showSafetyPrompt = false,
                showFollowUp = false,
                followUpQuestion = null,
                isAnswering = false,
                transcript = "",
                timerSeconds = 0,
                isTimerRunning = false
            )
            
            // If this question was from the star queue, remove it
            if (starTags.isNotEmpty() && question.tags.any { starTags.contains(it) }) {
                starTagsQueues[currentPlayer.id]?.poll()
            }
        }
    }
    
    /**
     * Skip the current question
     */
    fun skipQuestion() {
        viewModelScope.launch {
            val session = gameSession ?: return@launch
            val currentPlayer = getCurrentPlayer() ?: return@launch
            val currentQuestion = _uiState.value.currentQuestion ?: return@launch
            
            // Record skip in history
            questionHistoryRepository.recordQuestion(
                sessionId = session.id,
                playerId = currentPlayer.id,
                question = currentQuestion,
                wasSkipped = true
            )
            
            // Send learning signal
            learningAgent.updateSignals(
                LearningSignal(
                    playerId = currentPlayer.id,
                    type = SignalType.SKIP,
                    questionId = currentQuestion.id,
                    tags = currentQuestion.tags,
                    heat = null,
                    depth = currentQuestion.depthLevel
                )
            )
            
            // Move to next player
            nextPlayer()
            
            // Load next question
            loadNextQuestion()
        }
    }
    
    /**
     * Star the current question
     */
    fun starQuestion() {
        viewModelScope.launch {
            val currentQuestion = _uiState.value.currentQuestion ?: return@launch
            val questionId = _uiState.value.currentQuestionId ?: return@launch
            
            // Star the question in history
            questionHistoryRepository.starQuestion(questionId, true)
            
            // Add tags to star queue for future questions
            val currentPlayer = getCurrentPlayer() ?: return@launch
            val tagsToQueue = currentQuestion.tags.take(2) // Take up to 2 tags
            
            tagsToQueue.forEach { tag ->
                starTagsQueues[currentPlayer.id]?.offer(tag)
            }
            
            // Update UI state
            _uiState.value = _uiState.value.copy(
                isStarred = true,
                starredTags = tagsToQueue
            )
            
            // Send learning signal
            learningAgent.updateSignals(
                LearningSignal(
                    playerId = currentPlayer.id,
                    type = SignalType.INTEREST,
                    questionId = currentQuestion.id,
                    tags = tagsToQueue,
                    heat = null,
                    depth = currentQuestion.depthLevel
                )
            )
        }
    }
    
    /**
     * Start answering a question
     */
    fun startAnswering() {
        _uiState.value = _uiState.value.copy(
            isAnswering = true,
            transcript = "",
            timerSeconds = 0
        )
        
        // Start timer
        startTimer()
        
        // Start voice recognition if enabled
        if (_uiState.value.voiceModeEnabled) {
            startVoiceRecognition()
        }
    }
    
    /**
     * Finish answering a question
     */
    fun finishAnswering() {
        // Stop timer
        stopTimer()
        
        // Stop voice recognition
        stopVoiceRecognition()
        
        val transcript = _uiState.value.transcript
        
        viewModelScope.launch {
            val currentQuestion = _uiState.value.currentQuestion ?: return@launch
            val currentPlayer = getCurrentPlayer() ?: return@launch
            
            // Check for follow-up question
            if (transcript.isNotEmpty()) {
                val followUp = questionSelector.maybeAskFollowUp(transcript, 
                    SelectorContext(
                        playerId = currentPlayer.id,
                        mode = gameSession?.mode ?: return@launch,
                        heat = gameSession?.maxHeat ?: 50,
                        maxDepth = gameSession?.maxDepth ?: 1,
                        starTagsQueue = starTagsQueues[currentPlayer.id]?.toList() ?: emptyList(),
                        lastTags = currentQuestion.tags,
                        bias = playerPreferenceRepository.getProfileBiasForPlayerSync(currentPlayer.id),
                        mood = _uiState.value.currentMood
                    )
                )
                
                if (followUp != null) {
                    // Record follow-up in history
                    val followUpHistory = questionHistoryRepository.recordFollowUpQuestion(
                        originalQuestionId = _uiState.value.currentQuestionId ?: "",
                        followUpQuestion = followUp,
                        sessionId = gameSession?.id ?: "",
                        playerId = currentPlayer.id
                    )
                    
                    // Update UI state to show follow-up
                    _uiState.value = _uiState.value.copy(
                        showFollowUp = true,
                        followUpQuestion = followUp,
                        followUpQuestionId = followUpHistory.id,
                        isAnswering = false
                    )
                    
                    // Update story agent
                    storyAgent.onAsked(followUp, null)
                    
                    return@launch
                }
            }
            
            // If no follow-up, move to next player
            _uiState.value = _uiState.value.copy(isAnswering = false)
            nextPlayer()
            loadNextQuestion()
        }
    }
    
    /**
     * Skip the follow-up question
     */
    fun skipFollowUp() {
        viewModelScope.launch {
            val followUpQuestion = _uiState.value.followUpQuestion ?: return@launch
            val followUpId = _uiState.value.followUpQuestionId ?: return@launch
            val currentPlayer = getCurrentPlayer() ?: return@launch
            
            // Mark as skipped in history
            questionHistoryRepository.starQuestion(followUpId, false)
            
            // Send learning signal
            learningAgent.updateSignals(
                LearningSignal(
                    playerId = currentPlayer.id,
                    type = SignalType.SKIP,
                    questionId = followUpQuestion.id,
                    tags = followUpQuestion.tags,
                    heat = null,
                    depth = followUpQuestion.depthLevel
                )
            )
            
            // Move to next player
            nextPlayer()
            loadNextQuestion()
        }
    }
    
    /**
     * Answer the follow-up question
     */
    fun answerFollowUp() {
        _uiState.value = _uiState.value.copy(
            isAnswering = true,
            transcript = "",
            timerSeconds = 0,
            currentQuestion = _uiState.value.followUpQuestion,
            currentQuestionId = _uiState.value.followUpQuestionId,
            showFollowUp = false,
            followUpQuestion = null,
            followUpQuestionId = null
        )
        
        // Start timer
        startTimer()
        
        // Start voice recognition if enabled
        if (_uiState.value.voiceModeEnabled) {
            startVoiceRecognition()
        }
    }
    
    /**
     * Toggle voice mode
     */
    fun toggleVoiceMode() {
        val newVoiceModeEnabled = !_uiState.value.voiceModeEnabled
        
        viewModelScope.launch {
            appPreferences.setVoiceModeEnabled(newVoiceModeEnabled)
            _uiState.value = _uiState.value.copy(voiceModeEnabled = newVoiceModeEnabled)
            
            // Start or stop voice recognition if currently answering
            if (_uiState.value.isAnswering) {
                if (newVoiceModeEnabled) {
                    startVoiceRecognition()
                } else {
                    stopVoiceRecognition()
                }
            }
        }
    }
    
    /**
     * End the game
     */
    fun endGame() {
        viewModelScope.launch {
            val sessionId = gameSession?.id ?: return@launch
            
            // End the session
            gameSessionRepository.endSession(sessionId)
            
            // Clear current session ID
            appPreferences.setCurrentSessionId(null)
        }
    }
    
    /**
     * Start voice recognition
     */
    private fun startVoiceRecognition() {
        // Cancel any existing job
        voiceListeningJob?.cancel()
        
        // Start new listening job
        voiceListeningJob = viewModelScope.launch {
            voiceAgent.startListening().collectLatest { event ->
                processTranscript(event)
            }
        }
    }
    
    /**
     * Stop voice recognition
     */
    private fun stopVoiceRecognition() {
        voiceListeningJob?.cancel()
        voiceAgent.stopListening()
    }
    
    /**
     * Process a transcript event
     */
    private fun processTranscript(event: TranscriptEvent) {
        // Update transcript
        _uiState.value = _uiState.value.copy(
            transcript = event.text,
            transcriptConfidence = event.confidence
        )
        
        // Analyze emotion
        val emotion = emotionAgent.analyze(event)
        _uiState.value = _uiState.value.copy(
            currentMood = emotion.mood,
            stressLevel = emotion.stress
        )
        
        // Check for safety triggers
        viewModelScope.launch {
            val isAnswerSafe = safetyAgent.checkAnswer(event.text)
            if (!isAnswerSafe) {
                _uiState.value = _uiState.value.copy(
                    showSafetyPrompt = true
                )
                stopVoiceRecognition()
                stopTimer()
            }
        }
    }
    
    /**
     * Start the timer
     */
    private fun startTimer() {
        // Cancel any existing timer
        timerJob?.cancel()
        
        // Start new timer
        timerJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                timerSeconds = 0,
                isTimerRunning = true
            )
            
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    timerSeconds = _uiState.value.timerSeconds + 1
                )
            }
        }
    }
    
    /**
     * Stop the timer
     */
    private fun stopTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTimerRunning = false)
    }
    
    /**
     * Set the current player
     * @param index The player index
     */
    private fun setCurrentPlayer(index: Int) {
        if (index < 0 || index >= players.size) return
        
        currentPlayerIndex = index
        val player = players[index]
        
        _uiState.value = _uiState.value.copy(
            currentPlayer = player,
            isStarred = false,
            starredTags = emptyList()
        )
    }
    
    /**
     * Move to the next player
     */
    private fun nextPlayer() {
        val nextIndex = (currentPlayerIndex + 1) % players.size
        setCurrentPlayer(nextIndex)
    }
    
    /**
     * Get the current player
     * @return The current player, or null if no players
     */
    private fun getCurrentPlayer(): PlayerEntity? {
        return if (players.isNotEmpty()) players[currentPlayerIndex] else null
    }
    
    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        stopVoiceRecognition()
        stopTimer()
    }
}

/**
 * UI state for the game screen
 */
data class GameUiState(
    val currentPlayer: PlayerEntity? = null,
    val currentQuestion: Question? = null,
    val currentQuestionId: String? = null,
    val isAnswering: Boolean = false,
    val transcript: String = "",
    val transcriptConfidence: Float = 0f,
    val timerSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val showSafetyPrompt: Boolean = false,
    val showFollowUp: Boolean = false,
    val followUpQuestion: Question? = null,
    val followUpQuestionId: String? = null,
    val isStarred: Boolean = false,
    val starredTags: List<String> = emptyList(),
    val voiceModeEnabled: Boolean = false,
    val currentMood: Mood = Mood.CALM,
    val stressLevel: Int = 0
)