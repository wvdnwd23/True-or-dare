package com.truthordare.game.features.spotlight

import com.truthordare.game.models.*
import com.truthordare.game.repositories.GameSessionRepository
import com.truthordare.game.utils.RandomGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SpotlightMomentManager is responsible for detecting, triggering, and managing spotlight moments
 * during gameplay. It integrates with the game flow to create special memorable moments.
 */
class SpotlightMomentManager(
    private val gameSessionRepository: GameSessionRepository,
    private val randomGenerator: RandomGenerator
) {
    // Configuration parameters
    private val milestoneTurnIntervals = listOf(10, 25, 50)
    private val randomSpotlightProbability = 0.10f // 10% chance per turn
    private val minTurnsBetweenSpotlights = 3
    private val comebackThreshold = 3 // Turns without active participation

    // State tracking
    private var lastSpotlightTurn = 0
    private val _currentSpotlightMoment = MutableStateFlow<SpotlightMoment?>(null)
    val currentSpotlightMoment: StateFlow<SpotlightMoment?> = _currentSpotlightMoment.asStateFlow()

    /**
     * Check if the current turn should trigger a spotlight moment
     */
    fun checkForSpotlightMoment(gameState: GameState): SpotlightMoment? {
        // Don't trigger spotlight moments too frequently
        if (gameState.currentTurn - lastSpotlightTurn < minTurnsBetweenSpotlights) {
            return null
        }

        // Check for milestone moments
        if (isMilestoneTurn(gameState.currentTurn)) {
            return triggerSpotlightMoment(
                selectSpotlightMoment(SpotlightType.MILESTONE, gameState)
            )
        }
        
        // Check for category-based moments
        if (hasSpecialCategoryCombo(gameState.selectedCategories)) {
            return triggerSpotlightMoment(
                selectSpotlightMoment(SpotlightType.CATEGORY_BASED, gameState)
            )
        }
        
        // Check for comeback opportunities
        val inactivePlayer = findInactivePlayer(gameState.players)
        if (inactivePlayer != null && gameState.currentPlayer == inactivePlayer) {
            return triggerSpotlightMoment(
                selectSpotlightMoment(SpotlightType.COMEBACK, gameState)
            )
        }
        
        // Random chance for surprise moments
        if (shouldTriggerRandomMoment()) {
            return triggerSpotlightMoment(
                selectSpotlightMoment(SpotlightType.RANDOM, gameState)
            )
        }
        
        return null
    }

    /**
     * Determine if the current turn is a milestone turn
     */
    private fun isMilestoneTurn(currentTurn: Int): Boolean {
        return milestoneTurnIntervals.any { currentTurn % it == 0 }
    }

    /**
     * Check if the selected categories create a special combination
     */
    private fun hasSpecialCategoryCombo(categories: List<Category>): Boolean {
        // Special combinations that trigger spotlight moments
        val specialCombos = listOf(
            setOf("hot", "couples"),
            setOf("deep_past", "self_discovery"),
            setOf("mental_health", "self_growth"),
            setOf("extreme", "dirty")
        )
        
        val selectedCategoryIds = categories.map { it.id }.toSet()
        return specialCombos.any { combo -> selectedCategoryIds.containsAll(combo) }
    }

    /**
     * Find players who haven't been active recently
     */
    private fun findInactivePlayer(players: List<Player>): Player? {
        return players.find { player -> 
            player.turnsSinceLastAction >= comebackThreshold 
        }
    }

    /**
     * Determine if a random spotlight moment should be triggered
     */
    private fun shouldTriggerRandomMoment(): Boolean {
        return randomGenerator.nextFloat() < randomSpotlightProbability
    }

    /**
     * Select an appropriate spotlight moment based on type and game state
     */
    private fun selectSpotlightMoment(type: SpotlightType, gameState: GameState): SpotlightMoment {
        // Get spotlight moments matching the type
        val spotlightPool = spotlightMomentDatabase.filter { it.type == type }
        
        // Filter by applicable categories if needed
        val categoryFiltered = if (type == SpotlightType.CATEGORY_BASED) {
            val categoryIds = gameState.selectedCategories.map { it.id }
            spotlightPool.filter { moment ->
                moment.applicableCategories?.any { categoryIds.contains(it) } ?: false
            }
        } else {
            spotlightPool
        }
        
        // Select a random spotlight moment from the filtered pool
        return categoryFiltered.randomOrNull() 
            ?: spotlightPool.random() // Fallback if category filtering left no options
    }

    /**
     * Trigger a spotlight moment and update game state
     */
    private fun triggerSpotlightMoment(spotlightMoment: SpotlightMoment): SpotlightMoment {
        _currentSpotlightMoment.value = spotlightMoment
        lastSpotlightTurn = gameSessionRepository.getCurrentGameSession().currentTurn
        
        // Log the spotlight moment for analytics
        logSpotlightMoment(spotlightMoment)
        
        return spotlightMoment
    }

    /**
     * Complete the current spotlight moment and apply any effects
     */
    fun completeSpotlightMoment(result: SpotlightMomentResult) {
        val moment = _currentSpotlightMoment.value ?: return
        
        // Apply any game effects based on the spotlight moment result
        applySpotlightEffects(moment, result)
        
        // Clear the current spotlight moment
        _currentSpotlightMoment.value = null
        
        // Update game statistics
        updateSpotlightStatistics(moment, result)
    }

    /**
     * Apply effects to the game based on spotlight moment result
     */
    private fun applySpotlightEffects(moment: SpotlightMoment, result: SpotlightMomentResult) {
        val gameSession = gameSessionRepository.getCurrentGameSession()
        
        // Apply different effects based on the spotlight type and result
        when (moment.type) {
            SpotlightType.MILESTONE -> {
                // Milestone effects might include bonus points or special privileges
                if (result.completed) {
                    gameSession.players[result.playerId]?.let { player ->
                        player.score += 5 // Bonus points for completing milestone challenge
                    }
                }
            }
            SpotlightType.COMEBACK -> {
                // Comeback effects might include catching up mechanics
                if (result.completed) {
                    gameSession.players[result.playerId]?.let { player ->
                        player.turnsSinceLastAction = 0
                        player.comebackBonus = true // Flag for special treatment next turn
                    }
                }
            }
            SpotlightType.RANDOM -> {
                // Random effects might include changing game dynamics temporarily
                if (result.completed) {
                    gameSession.temporaryRuleModifiers.add(
                        RuleModifier(
                            type = result.effectType ?: RuleModifierType.STANDARD,
                            durationTurns = 3,
                            description = "Effect from ${moment.title}"
                        )
                    )
                }
            }
            SpotlightType.CATEGORY_BASED -> {
                // Category effects might include category-specific bonuses
                if (result.completed) {
                    gameSession.categoryBonuses[moment.applicableCategories?.firstOrNull() ?: ""] = 2
                }
            }
        }
        
        // Save the updated game session
        gameSessionRepository.updateGameSession(gameSession)
    }

    /**
     * Log spotlight moment for analytics
     */
    private fun logSpotlightMoment(moment: SpotlightMoment) {
        // Implementation for analytics logging
    }

    /**
     * Update game statistics related to spotlight moments
     */
    private fun updateSpotlightStatistics(moment: SpotlightMoment, result: SpotlightMomentResult) {
        val gameSession = gameSessionRepository.getCurrentGameSession()
        
        // Update spotlight statistics
        gameSession.spotlightStats.totalSpotlights++
        if (result.completed) {
            gameSession.spotlightStats.completedSpotlights++
        }
        
        gameSession.spotlightStats.spotlightsByType[moment.type.name] = 
            (gameSession.spotlightStats.spotlightsByType[moment.type.name] ?: 0) + 1
        
        // Save the updated game session
        gameSessionRepository.updateGameSession(gameSession)
    }

    companion object {
        // Database of spotlight moments
        val spotlightMomentDatabase = listOf(
            // Milestone Spotlight Moments
            SpotlightMoment(
                id = "milestone_1",
                type = SpotlightType.MILESTONE,
                title = "10th Turn Celebration",
                description = "You've reached the 10th turn! Time for a special challenge.",
                challenge = "The player must answer a truth AND complete a dare",
                reward = "Earn double points for this turn",
                visualEffect = VisualEffectType.CONFETTI,
                soundEffect = SoundEffectType.FANFARE,
                applicableCategories = null
            ),
            SpotlightMoment(
                id = "milestone_2",
                type = SpotlightType.MILESTONE,
                title = "Halfway Point",
                description = "You've reached the halfway point of the game!",
                challenge = "All players vote on who has been most entertaining so far",
                reward = "Winner gets to skip one challenge later",
                visualEffect = VisualEffectType.SPOTLIGHT,
                soundEffect = SoundEffectType.DRUMROLL,
                applicableCategories = null
            ),
            SpotlightMoment(
                id = "milestone_3",
                type = SpotlightType.MILESTONE,
                title = "Quarter Century",
                description = "25 turns completed! Time for a group challenge.",
                challenge = "Everyone must share one word that describes their experience so far",
                reward = "Next round is worth triple points",
                visualEffect = VisualEffectType.FIREWORKS,
                soundEffect = SoundEffectType.APPLAUSE,
                applicableCategories = null
            ),
            
            // Random Spotlight Moments
            SpotlightMoment(
                id = "random_1",
                type = SpotlightType.RANDOM,
                title = "Surprise Switch",
                description = "Unexpected twist!",
                challenge = "The current player can choose any other player to complete their turn instead",
                reward = null,
                visualEffect = VisualEffectType.PULSE,
                soundEffect = SoundEffectType.SURPRISE,
                applicableCategories = null
            ),
            SpotlightMoment(
                id = "random_2",
                type = SpotlightType.RANDOM,
                title = "Creative Control",
                description = "Time to get creative!",
                challenge = "The current player gets to create their own truth or dare for another player",
                reward = "Both players earn points if completed",
                visualEffect = VisualEffectType.GLOW,
                soundEffect = SoundEffectType.CHIME,
                applicableCategories = null
            ),
            SpotlightMoment(
                id = "random_3",
                type = SpotlightType.RANDOM,
                title = "Double Trouble",
                description = "Two challenges are better than one!",
                challenge = "The player must complete two consecutive challenges",
                reward = "Triple points if both are completed",
                visualEffect = VisualEffectType.CONFETTI,
                soundEffect = SoundEffectType.DRUMROLL,
                applicableCategories = null
            ),
            
            // Category-Based Spotlight Moments
            SpotlightMoment(
                id = "category_1",
                type = SpotlightType.CATEGORY_BASED,
                title = "Hot Streak",
                description = "Things are heating up!",
                challenge = "The next three turns must all be dares",
                reward = null,
                visualEffect = VisualEffectType.GLOW,
                soundEffect = SoundEffectType.SURPRISE,
                applicableCategories = listOf("hot", "extreme", "dirty")
            ),
            SpotlightMoment(
                id = "category_2",
                type = SpotlightType.CATEGORY_BASED,
                title = "Deep Reflection",
                description = "Time for some introspection.",
                challenge = "All players share a brief childhood memory",
                reward = null,
                visualEffect = VisualEffectType.SPOTLIGHT,
                soundEffect = SoundEffectType.CHIME,
                applicableCategories = listOf("deep_past", "self_discovery", "childhood")
            ),
            SpotlightMoment(
                id = "category_3",
                type = SpotlightType.CATEGORY_BASED,
                title = "Couples Connection",
                description = "Special moment for couples!",
                challenge = "Partners must complete a challenge together",
                reward = "Both earn bonus points",
                visualEffect = VisualEffectType.FIREWORKS,
                soundEffect = SoundEffectType.FANFARE,
                applicableCategories = listOf("couples", "couples_hot")
            ),
            
            // Comeback Spotlight Moments
            SpotlightMoment(
                id = "comeback_1",
                type = SpotlightType.COMEBACK,
                title = "Comeback Kid",
                description = "Your time to shine!",
                challenge = "Player who hasn't had a turn in a while gets a special power for their next turn",
                reward = "Choose any player to challenge next",
                visualEffect = VisualEffectType.SPOTLIGHT,
                soundEffect = SoundEffectType.APPLAUSE,
                applicableCategories = null
            ),
            SpotlightMoment(
                id = "comeback_2",
                type = SpotlightType.COMEBACK,
                title = "Redemption Round",
                description = "Everyone deserves a second chance!",
                challenge = "Player who skipped the last challenge gets a second chance with a bonus reward",
                reward = "Double points for completion",
                visualEffect = VisualEffectType.PULSE,
                soundEffect = SoundEffectType.DRUMROLL,
                applicableCategories = null
            ),
            SpotlightMoment(
                id = "comeback_3",
                type = SpotlightType.COMEBACK,
                title = "Spotlight Focus",
                description = "All eyes on you!",
                challenge = "The quietest player must be the center of attention for the next three turns",
                reward = "Triple points for all challenges completed",
                visualEffect = VisualEffectType.CONFETTI,
                soundEffect = SoundEffectType.FANFARE,
                applicableCategories = null
            )
        )
    }
}

/**
 * Represents a special moment in the game that gets highlighted
 */
data class SpotlightMoment(
    val id: String,
    val type: SpotlightType,
    val title: String,
    val description: String,
    val challenge: String,
    val reward: String?,
    val visualEffect: VisualEffectType,
    val soundEffect: SoundEffectType,
    val applicableCategories: List<String>?
)

/**
 * Types of spotlight moments
 */
enum class SpotlightType {
    MILESTONE,  // Based on turn count or game progress
    RANDOM,     // Randomly triggered
    CATEGORY_BASED, // Based on selected categories
    COMEBACK    // For players who need engagement
}

/**
 * Visual effect types for spotlight moments
 */
enum class VisualEffectType {
    CONFETTI,
    SPOTLIGHT,
    GLOW,
    FIREWORKS,
    PULSE
}

/**
 * Sound effect types for spotlight moments
 */
enum class SoundEffectType {
    FANFARE,
    DRUMROLL,
    CHIME,
    APPLAUSE,
    SURPRISE
}

/**
 * Result of a spotlight moment challenge
 */
data class SpotlightMomentResult(
    val spotlightId: String,
    val playerId: String,
    val completed: Boolean,
    val effectType: RuleModifierType? = null
)

/**
 * Types of rule modifiers that can be applied from spotlight moments
 */
enum class RuleModifierType {
    STANDARD,
    DOUBLE_POINTS,
    TRUTH_ONLY,
    DARE_ONLY,
    REVERSE_ORDER,
    TEAM_CHALLENGE
}

/**
 * Temporary rule modification applied to the game
 */
data class RuleModifier(
    val type: RuleModifierType,
    val durationTurns: Int,
    val description: String
)

/**
 * Statistics tracking for spotlight moments
 */
data class SpotlightStatistics(
    var totalSpotlights: Int = 0,
    var completedSpotlights: Int = 0,
    val spotlightsByType: MutableMap<String, Int> = mutableMapOf()
)

/**
 * Extension of GameState to include spotlight-related data
 */
data class GameState(
    val currentTurn: Int,
    val players: List<Player>,
    val currentPlayer: Player,
    val selectedCategories: List<Category>,
    val temporaryRuleModifiers: MutableList<RuleModifier> = mutableListOf(),
    val categoryBonuses: MutableMap<String, Int> = mutableMapOf(),
    val spotlightStats: SpotlightStatistics = SpotlightStatistics()
)

/**
 * Player data with spotlight-related fields
 */
data class Player(
    val id: String,
    val name: String,
    var score: Int = 0,
    var turnsSinceLastAction: Int = 0,
    var comebackBonus: Boolean = false
)