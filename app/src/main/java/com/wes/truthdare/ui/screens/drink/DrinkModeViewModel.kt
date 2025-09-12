package com.wes.truthdare.ui.screens.drink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wes.truthdare.core.data.DesignatedDriverManager
import com.wes.truthdare.core.data.SessionLimitsManager
import com.wes.truthdare.core.data.entities.PlayerEntity
import com.wes.truthdare.core.util.WaterReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the Drink Mode screen
 */
@HiltViewModel
class DrinkModeViewModel @Inject constructor(
    private val waterReminderManager: WaterReminderManager,
    private val designatedDriverManager: DesignatedDriverManager,
    private val sessionLimitsManager: SessionLimitsManager
) : ViewModel() {

    // UI State for Drink Mode
    private val _uiState = MutableStateFlow(DrinkModeUiState())
    val uiState: StateFlow<DrinkModeUiState> = _uiState.asStateFlow()
    
    // Water reminder state
    val waterReminderState = waterReminderManager.reminderState
    
    // Designated driver ID
    val designatedDriverId = designatedDriverManager.designatedDriverId
    
    // Session limits
    val timeLimitMinutes = sessionLimitsManager.timeLimitMinutes
    val drinkLimitCount = sessionLimitsManager.drinkLimitCount
    val remainingTimeMinutes = sessionLimitsManager.getRemainingTimeMinutes()
    val isTimeLimitReached = sessionLimitsManager.isTimeLimitReached()

    // Initialize with default values
    init {
        val allMiniGames = generateMiniGames()
        _uiState.update { currentState ->
            currentState.copy(
                drinkingRules = generateDefaultDrinkingRules(),
                miniGames = allMiniGames,
                filteredMiniGames = allMiniGames // Initially, all mini-games are shown
            )
        }
        
        // Start water reminders (default 20 minutes)
        startWaterReminders()
        
        // Start session timer
        viewModelScope.launch {
            sessionLimitsManager.startSession()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        waterReminderManager.stopReminders()
        
        viewModelScope.launch {
            sessionLimitsManager.endSession()
        }
    }

    /**
     * Increment the shot counter
     */
    fun incrementShotCounter() {
        _uiState.update { currentState ->
            currentState.copy(shotCount = currentState.shotCount + 1)
        }
    }

    /**
     * Decrement the shot counter
     */
    fun decrementShotCounter() {
        _uiState.update { currentState ->
            currentState.copy(shotCount = (currentState.shotCount - 1).coerceAtLeast(0))
        }
    }

    /**
     * Reset the shot counter to zero
     */
    fun resetShotCounter() {
        _uiState.update { currentState ->
            currentState.copy(shotCount = 0)
        }
    }

    /**
     * Use a lie card (reduces the remaining count)
     */
    fun useLieCard() {
        _uiState.update { currentState ->
            if (currentState.remainingLieCards > 0) {
                currentState.copy(remainingLieCards = currentState.remainingLieCards - 1)
            } else {
                currentState
            }
        }
    }

    /**
     * Reset lie cards to the default amount
     */
    fun resetLieCards() {
        _uiState.update { currentState ->
            currentState.copy(remainingLieCards = DEFAULT_LIE_CARDS)
        }
    }

    /**
     * Generate a random mini-game from the available list
     */
    fun generateRandomMiniGame() {
        val miniGames = _uiState.value.filteredMiniGames.ifEmpty { _uiState.value.miniGames }
        if (miniGames.isNotEmpty()) {
            val randomIndex = Random.nextInt(miniGames.size)
            _uiState.update { currentState ->
                currentState.copy(currentMiniGame = miniGames[randomIndex])
            }
        }
    }
    
    /**
     * Start water reminders
     * @param intervalMinutes The interval in minutes between reminders
     */
    fun startWaterReminders(intervalMinutes: Int = 20) {
        waterReminderManager.startReminders(intervalMinutes)
    }
    
    /**
     * Stop water reminders
     */
    fun stopWaterReminders() {
        waterReminderManager.stopReminders()
    }
    
    /**
     * Dismiss the current water reminder and start a new timer
     */
    fun dismissWaterReminderAndRemindLater() {
        waterReminderManager.dismissAndRemindLater()
    }
    
    /**
     * Dismiss the current water reminder without starting a new timer
     */
    fun dismissWaterReminder() {
        waterReminderManager.dismissReminder()
    }
    
    /**
     * Set the designated driver
     * @param playerId The ID of the player who is the designated driver
     */
    fun setDesignatedDriver(playerId: String?) {
        viewModelScope.launch {
            designatedDriverManager.setDesignatedDriver(playerId)
        }
    }
    
    /**
     * Clear the designated driver
     */
    fun clearDesignatedDriver() {
        viewModelScope.launch {
            designatedDriverManager.clearDesignatedDriver()
        }
    }
    
    /**
     * Set the time limit in minutes
     * @param minutes The time limit in minutes
     */
    fun setTimeLimit(minutes: Int?) {
        viewModelScope.launch {
            sessionLimitsManager.setTimeLimit(minutes)
        }
    }
    
    /**
     * Set the drink limit count
     * @param count The drink limit count
     */
    fun setDrinkLimit(count: Int?) {
        viewModelScope.launch {
            sessionLimitsManager.setDrinkLimit(count)
        }
    }
    
    /**
     * Clear all session limits
     */
    fun clearSessionLimits() {
        viewModelScope.launch {
            sessionLimitsManager.clearLimits()
        }
    }
    
    /**
     * Check if a player is the designated driver
     * @param playerId The ID of the player to check
     * @return Flow of Boolean indicating if the player is the designated driver
     */
    fun isDesignatedDriver(playerId: String): Flow<Boolean> {
        return designatedDriverManager.isDesignatedDriver(playerId)
    }
    
    /**
     * Show water reminder dialog
     */
    fun showWaterReminderDialog() {
        _uiState.update { currentState ->
            currentState.copy(showWaterReminder = true)
        }
    }
    
    /**
     * Hide water reminder dialog
     */
    fun hideWaterReminderDialog() {
        _uiState.update { currentState ->
            currentState.copy(showWaterReminder = false)
        }
    }
    
    /**
     * Show emergency contacts dialog
     */
    fun showEmergencyContactsDialog() {
        _uiState.update { currentState ->
            currentState.copy(showEmergencyContacts = true)
        }
    }
    
    /**
     * Hide emergency contacts dialog
     */
    fun hideEmergencyContactsDialog() {
        _uiState.update { currentState ->
            currentState.copy(showEmergencyContacts = false)
        }
    }
    
    /**
     * Show session limits dialog
     */
    fun showSessionLimitsDialog() {
        _uiState.update { currentState ->
            currentState.copy(showSessionLimits = true)
        }
    }
    
    /**
     * Hide session limits dialog
     */
    fun hideSessionLimitsDialog() {
        _uiState.update { currentState ->
            currentState.copy(showSessionLimits = false)
        }
    }
    
    /**
     * Show designated driver dialog
     */
    fun showDesignatedDriverDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDesignatedDriver = true)
        }
    }
    
    /**
     * Hide designated driver dialog
     */
    fun hideDesignatedDriverDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDesignatedDriver = false)
        }
    }
    
    /**
     * Dismiss time limit reached alert
     */
    fun dismissTimeLimitAlert() {
        _uiState.update { currentState ->
            currentState.copy(showTimeLimitReachedAlert = false)
        }
    }
    
    /**
     * Dismiss drink limit reached alert
     */
    fun dismissDrinkLimitAlert() {
        _uiState.update { currentState ->
            currentState.copy(showDrinkLimitReachedAlert = false)
        }
    }
    
    /**
     * Set the selected difficulty filter
     * @param difficulty The difficulty to filter by, or null for all difficulties
     */
    fun setDifficultyFilter(difficulty: Difficulty?) {
        _uiState.update { currentState ->
            val filteredGames = if (difficulty == null) {
                // If no difficulty is selected, show all mini-games
                currentState.miniGames
            } else {
                // Filter mini-games by the selected difficulty
                currentState.miniGames.filter { it.difficulty == difficulty }
            }
            
            currentState.copy(
                selectedDifficulty = difficulty,
                filteredMiniGames = filteredGames
            )
        }
    }
    
    /**
     * Generate a random mini-game with the current difficulty filter
     */
    fun generateRandomFilteredMiniGame() {
        val filteredGames = _uiState.value.filteredMiniGames
        if (filteredGames.isNotEmpty()) {
            val randomIndex = Random.nextInt(filteredGames.size)
            _uiState.update { currentState ->
                currentState.copy(currentMiniGame = filteredGames[randomIndex])
            }
        }
    }
    
    /**
     * Show difficulty filter dialog
     */
    fun showDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = true)
        }
    }
    
    /**
     * Hide difficulty filter dialog
     */
    fun hideDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = false)
        }
    }
    
    /**
     * Show difficulty filter dialog
     */
    fun showDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = true)
        }
    }
    
    /**
     * Hide difficulty filter dialog
     */
    fun hideDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = false)
        }
    }
    
    /**
     * Show difficulty filter dialog
     */
    fun showDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = true)
        }
    }
    
    /**
     * Hide difficulty filter dialog
     */
    fun hideDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = false)
        }
    }
    
    /**
     * Set the selected difficulty for filtering mini-games
     * @param difficulty The difficulty to filter by, or null to show all difficulties
     */
    fun setSelectedDifficulty(difficulty: Difficulty?) {
        _uiState.update { currentState ->
            val filteredGames = if (difficulty == null) {
                // If no difficulty is selected, show all mini-games
                currentState.miniGames
            } else {
                // Filter mini-games by the selected difficulty
                currentState.miniGames.filter { it.difficulty == difficulty }
            }
            
            currentState.copy(
                selectedDifficulty = difficulty,
                filteredMiniGames = filteredGames
            )
        }
    }
    
    /**
     * Generate a random mini-game from the filtered list
     */
    fun generateRandomFilteredMiniGame() {
        val filteredGames = _uiState.value.filteredMiniGames
        if (filteredGames.isNotEmpty()) {
            val randomIndex = Random.nextInt(filteredGames.size)
            _uiState.update { currentState ->
                currentState.copy(currentMiniGame = filteredGames[randomIndex])
            }
        }
    }
    
    /**
     * Show difficulty filter dialog
     */
    fun showDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = true)
        }
    }
    
    /**
     * Hide difficulty filter dialog
     */
    fun hideDifficultyFilterDialog() {
        _uiState.update { currentState ->
            currentState.copy(showDifficultyFilterDialog = false)
        }
    }
    
    /**
     * Show game instructions dialog
     */
    fun showGameInstructionsDialog() {
        _uiState.update { currentState ->
            currentState.copy(showGameInstructionsDialog = true)
        }
    }
    
    /**
     * Hide game instructions dialog
     */
    fun hideGameInstructionsDialog() {
        _uiState.update { currentState ->
            currentState.copy(showGameInstructionsDialog = false)
        }
    }

    /**
     * Generate default drinking rules
     */
    private fun generateDefaultDrinkingRules(): List<DrinkingRule> {
        return listOf(
            DrinkingRule(
                "Truth Penalty",
                "If you refuse to answer a Truth, take 2 shots."
            ),
            DrinkingRule(
                "Dare Penalty",
                "If you refuse to perform a Dare, take 3 shots."
            ),
            DrinkingRule(
                "Lie Card",
                "Each player starts with 3 Lie Cards. Use one to avoid answering truthfully."
            ),
            DrinkingRule(
                "Caught Lying",
                "If caught lying without using a Lie Card, take 4 shots."
            ),
            DrinkingRule(
                "Mini-Game Loser",
                "The loser of a mini-game takes 1 shot."
            ),
            DrinkingRule(
                "Phone Check",
                "If you check your phone during the game, take 1 shot."
            ),
            DrinkingRule(
                "Name Slip",
                "If you say someone's name instead of 'the person to my left/right', take 1 shot."
            ),
            DrinkingRule(
                "Bathroom Break",
                "Take 1 shot if you need a bathroom break (except for emergencies)."
            ),
            DrinkingRule(
                "Laugh Control",
                "If you laugh during a serious truth question, take 1 shot."
            ),
            DrinkingRule(
                "Drink Spill",
                "If you spill your drink, take 2 shots as penalty."
            ),
            DrinkingRule(
                "Hydration Rule",
                "Drink a glass of water after every 3 alcoholic drinks."
            ),
            DrinkingRule(
                "Designated Driver",
                "The designated driver doesn't drink alcohol and gets to assign 3 shots during the game."
            )
        )
    }

    /**
     * Generate a list of mini-games
     */
    private fun generateMiniGames(): List<MiniGame> {
        return listOf(
            // Original 20 mini-games
            MiniGame(
                "Never Have I Ever",
                "Players take turns saying 'Never have I ever...' followed by something they've never done. Anyone who has done it takes a drink.",
                Difficulty.EASY
            ),
            MiniGame(
                "Categories",
                "Choose a category (e.g., car brands). Go around the circle naming items in that category. First to hesitate or repeat takes a shot.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Word Association",
                "First player says a word, next player must say a related word. Continue until someone hesitates (3 seconds) or repeats a word.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Flip Cup",
                "Divide into two teams. Each player drinks and then tries to flip their cup upside down. First team to finish wins.",
                Difficulty.HARD
            ),
            MiniGame(
                "Most Likely To",
                "Someone asks 'Who's most likely to...?' Everyone points at who they think is most likely. Person with most points drinks that many shots.",
                Difficulty.EASY
            ),
            MiniGame(
                "Beer Pong",
                "Set up cups in a triangle at each end of a table. Take turns throwing ping pong balls into opponent's cups. If it lands, they drink.",
                Difficulty.HARD
            ),
            MiniGame(
                "Kings Cup",
                "Place a cup in the center surrounded by a circle of cards. Take turns drawing cards, each with a different rule.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Ride the Bus",
                "A multi-round card game where players guess card values. Wrong guesses mean drinks.",
                Difficulty.HARD
            ),
            MiniGame(
                "Quarters",
                "Bounce a quarter into a shot glass. If you make it, choose someone to drink.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Waterfall",
                "Everyone starts drinking at the same time. No one can stop until the person to their right stops.",
                Difficulty.EASY
            ),
            MiniGame(
                "Straight Face",
                "Players take turns trying to make others laugh while they try to keep a straight face. First to laugh drinks.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Rhyme Time",
                "First player says a word, next player must say a word that rhymes. Continue until someone can't think of a rhyme.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Buzz",
                "Count around the circle, replacing any number containing 7 or divisible by 7 with 'buzz'. Mess up and drink.",
                Difficulty.HARD
            ),
            MiniGame(
                "Thumb Master",
                "Designated player can put their thumb on the table anytime. Last player to notice and put their thumb down drinks.",
                Difficulty.EASY
            ),
            MiniGame(
                "Medusa",
                "Everyone looks down. On count of 3, look up at someone. If they're looking at you too, both drink.",
                Difficulty.EASY
            ),
            MiniGame(
                "Alphabet Game",
                "Choose a category. First player says something in that category starting with A, next with B, and so on.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Coin Flip",
                "Predict heads or tails. If wrong, drink. If right, give out a drink.",
                Difficulty.EASY
            ),
            MiniGame(
                "Staring Contest",
                "Two players stare at each other. First to blink or laugh drinks.",
                Difficulty.EASY
            ),
            MiniGame(
                "Rock Paper Scissors",
                "Loser of each round drinks. Play best of 3 or 5.",
                Difficulty.EASY
            ),
            MiniGame(
                "Chandelier",
                "Center cup surrounded by outer cups. Land ball in center, everyone drinks. Land in outer cup, assign that drink.",
                Difficulty.HARD
            ),
            
            // New mini-games (20 more) - Adding from mini_games_plan.md
            
            // Easy Difficulty (7 games)
            MiniGame(
                "Word Scramble",
                "Unscramble drinking-related words. Last to solve drinks. Words are shown scrambled and players race to figure out what they are.",
                Difficulty.EASY
            ),
            MiniGame(
                "Truth Chain",
                "Each player adds a 'truth' to a growing sentence. First to hesitate or repeat a word drinks. The sentence must remain coherent.",
                Difficulty.EASY
            ),
            MiniGame(
                "Dare Roulette",
                "Spin a virtual wheel to determine a random dare. Refuse and drink. Everyone takes turns spinning the wheel.",
                Difficulty.EASY
            ),
            MiniGame(
                "Tongue Twisters",
                "Say a tongue twister three times fast. Mess up and drink. Each player gets a different tongue twister of increasing difficulty.",
                Difficulty.EASY
            ),
            MiniGame(
                "Counting Game",
                "Count from 1, replacing certain numbers with words (e.g., multiples of 3 with 'Fizz'). Mess up and drink.",
                Difficulty.EASY
            ),
            MiniGame(
                "Compliment Circle",
                "Give a genuine compliment to another player. Fail to be original and drink. No repeating compliments that have already been given.",
                Difficulty.EASY
            ),
            MiniGame(
                "Accent Challenge",
                "Say a phrase in a specific accent. Group votes if it's good enough or drink. Each player gets a different accent and phrase.",
                Difficulty.EASY
            ),
            
            // Medium Difficulty (7 games)
            MiniGame(
                "Drink Trivia",
                "Answer alcohol-related trivia questions. Wrong answer means drink. Questions range from cocktail ingredients to drinking history.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Song Lyrics",
                "Continue the lyrics when the song stops. Fail and drink. Songs can be from any genre and era.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Movie Quotes",
                "Identify the movie from a famous quote. Wrong answer means drink. Quotes range from classic to modern films.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Two Truths, One Lie",
                "Tell two truths and one lie. If others guess the lie, you drink. If they can't identify the lie, they drink.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Jenga",
                "Take turns removing blocks from a tower. Topple it and drink. Some blocks have special rules written on them.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Celebrity",
                "Describe a celebrity without saying their name. Fail to get others to guess and drink. No gestures allowed, only words.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Dice",
                "Roll two dice. If you roll doubles, give out drinks equal to the sum. If you roll a 7, take a drink yourself.",
                Difficulty.MEDIUM
            ),
            
            // Hard Difficulty (6 games)
            MiniGame(
                "Speed Bartender",
                "Name as many cocktails as possible in 30 seconds. Lowest count drinks. No repeating cocktails that have already been named.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Pong Variations",
                "Special rules for beer pong with added challenges like using non-dominant hand, standing on one foot, or eyes closed.",
                Difficulty.HARD
            ),
            MiniGame(
                "Flip Cup Tournament",
                "Tournament-style flip cup with elimination rounds. Losers of each round drink and are eliminated until one winner remains.",
                Difficulty.HARD
            ),
            MiniGame(
                "Shot Roulette",
                "Spin to determine who takes a shot from a selection of mixed drinks. Some drinks might be non-alcoholic, others very strong.",
                Difficulty.HARD
            ),
            MiniGame(
                "Power Hour",
                "Take a small drink every minute for an hour (simulated in-game). Players who miss their minute must take a double drink.",
                Difficulty.HARD
            ),
            MiniGame(
                "Stack Attack",
                "Stack cups in a specific pattern while timed. Fail and drink. Patterns get increasingly complex with each round.",
                Difficulty.HARD
            )
        )
    }

    companion object {
        const val DEFAULT_LIE_CARDS = 3
    }
}

/**
 * UI State for Drink Mode
 */
data class DrinkModeUiState(
    val shotCount: Int = 0,
    val remainingLieCards: Int = DrinkModeViewModel.DEFAULT_LIE_CARDS,
    val drinkingRules: List<DrinkingRule> = emptyList(),
    val miniGames: List<MiniGame> = emptyList(),
    val filteredMiniGames: List<MiniGame> = emptyList(),
    val currentMiniGame: MiniGame? = null,
    val selectedDifficulty: Difficulty? = null, // null means all difficulties
    
    // Dialog visibility states
    val showWaterReminder: Boolean = false,
    val showEmergencyContacts: Boolean = false,
    val showSessionLimits: Boolean = false,
    val showDesignatedDriver: Boolean = false,
    val showTimeLimitReachedAlert: Boolean = false,
    val showDrinkLimitReachedAlert: Boolean = false,
    val showDifficultyFilterDialog: Boolean = false,
    val showGameInstructionsDialog: Boolean = false,
    
    // Responsible drinking features
    val designatedDriverId: String? = null,
    val timeLimitMinutes: Int? = null,
    val drinkLimitCount: Int? = null,
    val remainingTimeMinutes: Int? = null,
    val players: List<PlayerEntity> = emptyList()
)

/**
 * Data class for Drinking Rules
 */
data class DrinkingRule(
    val title: String,
    val description: String
)

/**
 * Data class for Mini-Games
 */
data class MiniGame(
    val title: String,
    val description: String,
    val difficulty: Difficulty
)

/**
 * Enum for Mini-Game difficulty levels
 */
enum class Difficulty {
    EASY, MEDIUM, HARD
}