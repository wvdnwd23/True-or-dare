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
            
            // New mini-games (80 more)
            
            // Easy Difficulty (27 new games)
            MiniGame(
                "Drink If",
                "The leader makes &quot;Drink if...&quot; statements. Anyone who fits the description drinks. Each player takes turns being the leader.",
                Difficulty.EASY
            ),
            MiniGame(
                "Rhyme Master",
                "First player says a word, next player must say a word that rhymes. Continue until someone can't think of a rhyme within 5 seconds.",
                Difficulty.EASY
            ),
            MiniGame(
                "Sentence Building",
                "Each player adds one word to build a sentence. If the sentence becomes nonsensical, the player drinks. The sentence must remain grammatically correct.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Race",
                "On the count of three, everyone drinks their beverage. Last to finish drinks again. Drinks should be approximately the same amount for fairness.",
                Difficulty.EASY
            ),
            MiniGame(
                "Spelling Bee",
                "Players take turns spelling increasingly difficult words. Misspell and drink. Each round increases in difficulty with 10 seconds to spell each word.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Pong Solo",
                "Set up cups and take turns trying to land ping pong balls. Miss and drink. Use 3-6 cups in a triangle formation with 2 attempts per turn.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Dice",
                "Roll a die. Drink the number shown. Roll a six for a free pass. The number rolled equals the number of sips to take.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink or Dare",
                "Choose to either drink or perform a dare chosen by the group. If you refuse the dare, you must drink double.",
                Difficulty.EASY
            ),
            MiniGame(
                "Thumb War",
                "Play thumb war with another player. Loser drinks. Best 2 out of 3 wins with no using your other hand to interfere.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Roulette",
                "Place cups in a circle with one containing a stronger drink. Spin and drink what's in front of you. Use 6-8 cups in a circle.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Limbo",
                "Set up a limbo bar. If you can't go under without touching, drink. Lower the bar after everyone successfully completes a round.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Charades",
                "Act out a drinking-related word or phrase. If no one guesses correctly, you drink. 60-second time limit for guessing with no speaking.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Pictionary",
                "Draw a drinking-related word or phrase. If no one guesses correctly, you drink. 60-second time limit with no letters or symbols.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Simon Says",
                "Play Simon Says with drinking penalties for mistakes. If you follow a command not preceded by &quot;Simon says,&quot; drink.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Memory",
                "Place cards face down. Match pairs or drink. Use 10-20 cards (5-10 pairs) and take turns flipping two cards.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Hot Potato",
                "Pass an object around while music plays. When it stops, holder drinks. Use a random music player with stop/pause function.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Telephone",
                "Whisper a message around the circle. If the final message is wrong, everyone drinks. First player creates a message at least 10 words long.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Trivia",
                "Answer trivia questions. Wrong answers drink. Use trivia apps or cards if available with 15 seconds to answer each question.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Karaoke",
                "Sing a portion of a song. Group votes if it's good enough or drink. Use a music app to play 30 seconds of a song.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Riddles",
                "Solve riddles. If wrong, drink. 30 seconds to solve each riddle with one hint allowed (costs a sip).",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Rock Paper Scissors",
                "Play rock paper scissors. Loser drinks. Best 2 out of 3 wins with winner challenging a new player.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Jokes",
                "Tell a joke. If no one laughs, you drink. Jokes must be original or not told yet in the game.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Categories",
                "Name items in a category. Repeat or hesitate and drink. 5-second time limit per player with no repeating items.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Opposites",
                "Leader says a word, you must say its opposite. Wrong answer drinks. 3-second time limit to respond.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Directions",
                "Leader says &quot;left&quot; or &quot;right&quot; and points. If they don't match, drink if you follow the pointing instead of the words.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Rhythm",
                "Maintain a clapping rhythm. Miss the beat and drink. Start with a simple rhythm and each player adds one beat to the sequence.",
                Difficulty.EASY
            ),
            MiniGame(
                "Drink Balance",
                "Balance an object on your head or finger. Drop it and drink. Start with 30 seconds of balancing and increase time by 15 seconds each round.",
                Difficulty.EASY
            ),
            
            // Medium Difficulty (27 new games)
            MiniGame(
                "Drink Poker",
                "Play a round of poker. Lowest hand drinks. Use a simplified 5-card draw poker with one round of discarding and drawing.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Blackjack",
                "Play a round of blackjack. Losers drink. One player acts as dealer with standard blackjack rules applying.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink War",
                "Play the card game War. Loser of each battle drinks. Deal equal cards to two players who flip top card simultaneously.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Quarters",
                "Bounce quarters into cups. Miss and drink. Place cup 1-2 feet away with each player getting 2 attempts per turn.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Flip Cup Relay",
                "Teams race to flip cups. Losing team drinks. Divide into equal teams lined up along a table.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Categories Advanced",
                "Name items in increasingly specific categories. Fail and drink. Start with broad category and narrow it each round.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Word Association Chain",
                "Say a word related to the previous word. Break the chain and drink. First player says any word with next player saying related word within 3 seconds.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Tongue Twisters",
                "Say increasingly difficult tongue twisters. Mess up and drink. Start with simple tongue twisters and increase difficulty each round.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Counting",
                "Count from 1, replacing multiples of 7 and numbers containing 7 with &quot;drink&quot;. Mess up and drink. Go around the circle counting up.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Spelling Backwards",
                "Spell words backwards. Fail and drink. Start with 4-letter words and increase length each round.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Blindfolded Taste Test",
                "Identify drinks while blindfolded. Wrong guess means drink. One player prepares 3-5 different drinks for tasting.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Pyramid",
                "Draw cards and drink based on a pyramid structure. Build a pyramid of face-down cards (e.g., 5-4-3-2-1) and draw from remaining deck.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Battleship",
                "Play a simplified version of Battleship. When your ship is hit, drink. Use a 5x5 grid with 3 ships per player.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Taboo",
                "Describe a word without using certain related words. Fail and drink. 60 seconds to get teammates to guess the word.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Jenga",
                "Play Jenga with drinking rules on blocks. Write drinking rules on Jenga blocks and follow the rule on the pulled block.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Dominoes",
                "Play dominoes. When you can't place a domino, drink. Deal 7 dominoes to each player with standard rules.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Darts",
                "Throw darts at a board. Miss the board and drink. Each player throws 3 darts per turn.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Dice Poker",
                "Roll five dice. Lowest poker hand drinks. Each player rolls 5 dice with one re-roll allowed.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Yahtzee",
                "Roll dice for Yahtzee combinations. Lowest score drinks. Each player gets 5 dice and up to 3 rolls.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Scattergories",
                "Write words starting with a specific letter in different categories. Duplicates drink. Choose 5 categories and 1 letter.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Hangman",
                "Play Hangman. If the word is guessed, the word creator drinks. If not, the guessers drink. 6 wrong guesses allowed.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Tic Tac Toe Tournament",
                "Play Tic Tac Toe. Losers drink. Organize a tournament bracket with standard rules.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Connect Four",
                "Play Connect Four. Loser drinks. Use a Connect Four game or app with standard rules.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Checkers",
                "Play a quick game of checkers. Loser drinks. Use a checkers board or app with 6 pieces each for quicker games.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Chess",
                "Play speed chess with a timer. Loser drinks. Use a chess board or app with 30-second timer per move.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Backgammon",
                "Play a simplified version of backgammon. Loser drinks. Use a backgammon board or app with simplified rules.",
                Difficulty.MEDIUM
            ),
            MiniGame(
                "Drink Uno",
                "Play a round of Uno. Loser drinks based on cards left in hand. Standard Uno rules apply with special card effects.",
                Difficulty.MEDIUM
            ),
            
            // Hard Difficulty (26 new games)
            MiniGame(
                "Drink Kings Cup",
                "Play Kings Cup with complex rules and drinking penalties. Place a cup in center surrounded by a circle of cards, each with specific rules.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Beer Pong Tournament",
                "Play a tournament-style beer pong with elimination rounds. Organize teams of 2 with standard beer pong rules and 6-10 cups.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Flip Cup Championship",
                "Multi-round flip cup tournament with increasing difficulty. Divide into equal teams with progressively harder rounds.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Quarters Master",
                "Bounce quarters into increasingly smaller targets. Miss and drink. Start with large cup and move to smaller cups with successful shots.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Trivia Gauntlet",
                "Answer progressively harder trivia questions. Wrong answers mean more drinks. 5 rounds of increasing difficulty.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Shot Roulette",
                "Spin to determine who takes a shot from a selection of mixed drinks. Arrange 8-10 shots in circle with 1-2 being stronger.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Power Hour",
                "Take a small drink every minute for an hour (simulated in-game). Set timer for 1-minute intervals with everyone drinking at each interval.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Stack Attack",
                "Stack cups in a specific pattern while timed. Fail and drink. Start with pyramid of 6 cups and 30 seconds to unstack and restack.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Speed Bartender",
                "Name as many cocktails as possible in 30 seconds. Lowest count drinks. Players take turns with no repeating cocktails already named.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Pong Variations",
                "Beer pong with special rules like behind-the-back shots, non-dominant hand, etc. Standard setup with special rules each round.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Obstacle Course",
                "Complete a mini obstacle course while holding a drink. Spill and drink double. Create simple obstacles with 60-second time limit.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Card House",
                "Build a house of cards. If it falls, drink based on how many cards were used. Each player adds one card at a time.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Dice Tower",
                "Stack dice on top of each other. If they fall, drink based on the sum. Start with 1 die and add 1 each turn.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Blind Bartender",
                "Mix a cocktail blindfolded. Others rate it. Low scores drink. Blindfold player and provide ingredients with 2 minutes to mix.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Bottle Flip Challenge",
                "Flip bottles to land upright. Miss and drink. Each player gets 3 attempts per round with partially filled bottles.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Coin Stack",
                "Stack coins on edge. If they fall, drink based on how many were stacked. Each player adds one coin at a time on edge.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Shot Ski",
                "Multiple players drink from attached shot glasses simultaneously. Last to finish drinks again. Attach 3+ shot glasses to board/ruler.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Chandelier",
                "Complex cup arrangement where landing a ball in the center means everyone drinks. Arrange cups in circle with center cup.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Boat Race",
                "Teams race to finish a line of drinks. Losing team drinks an extra penalty. Divide into equal teams with lined up cups.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Rage Cage",
                "Fast-paced game with multiple cups, dice, and drinking penalties. Arrange cups in circle with center cup and two players starting.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Civil War",
                "Team-based beer pong with multiple cups and complex rules. Two teams with 2+ players each and multiple balls in play.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Anchorman",
                "Last person to put their thumb on the table when the leader does drinks. One player is the Anchorman who subtly puts thumb on table.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Wizard Staff",
                "Stack empty cans/bottles to create a &quot;staff&quot;. Shortest staff drinks. Each finished drink adds to your staff by taping containers.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Centurion",
                "Take a shot of beer every minute for 100 minutes (simulated in-game). Compressed version: 10 shots over 10 minutes.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Battleshots",
                "Battleship game where hits mean drinks. Create grid with shot glasses and place &quot;ships&quot; of 2-5 connected shots.",
                Difficulty.HARD
            ),
            MiniGame(
                "Drink Thumper",
                "Complex rhythm game with personalized gestures. Miss your cue and drink. Each player creates personal gesture with leader setting rhythm.",
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