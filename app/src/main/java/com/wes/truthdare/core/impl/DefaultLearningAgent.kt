package com.wes.truthdare.core.impl

import com.wes.truthdare.core.agents.LearningAgent
import com.wes.truthdare.core.agents.LearningSignal
import com.wes.truthdare.core.agents.SignalType
import com.wes.truthdare.core.data.repositories.PlayerPreferenceRepository
import com.wes.truthdare.core.selector.ProfileBias
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of LearningAgent
 */
@Singleton
class DefaultLearningAgent @Inject constructor(
    private val playerPreferenceRepository: PlayerPreferenceRepository
) : LearningAgent {
    // Cache of player biases to avoid frequent database access
    private val biasCache = mutableMapOf<String, ProfileBias>()
    
    /**
     * Update the learning model with new signals
     * @param sig The learning signal to process
     */
    override fun updateSignals(sig: LearningSignal) {
        // Get current bias for the player
        val currentBias = biasCache[sig.playerId] ?: runBlocking {
            playerPreferenceRepository.getProfileBiasForPlayerSync(sig.playerId)
        }
        
        // Update the bias based on the signal type
        val updatedBias = when (sig.type) {
            SignalType.INTEREST -> handleInterestSignal(currentBias, sig)
            SignalType.DISCOMFORT -> handleDiscomfortSignal(currentBias, sig)
            SignalType.SKIP -> handleSkipSignal(currentBias, sig)
            SignalType.ENGAGEMENT -> handleEngagementSignal(currentBias, sig)
            SignalType.LAUGHTER -> handleLaughterSignal(currentBias, sig)
            SignalType.SILENCE -> handleSilenceSignal(currentBias, sig)
        }
        
        // Update the cache
        biasCache[sig.playerId] = updatedBias
        
        // Persist the updated bias
        runBlocking {
            persistBias(sig.playerId, updatedBias)
        }
    }
    
    /**
     * Get the current bias profile based on learning
     * @return The current ProfileBias
     */
    override fun currentBias(): ProfileBias {
        // This would typically use the current player ID from the game context
        // For now, return a default bias
        return ProfileBias(
            tagWeights = emptyMap(),
            depthComfort = 1,
            heatComfort = 50
        )
    }
    
    /**
     * Handle an interest signal
     * @param currentBias The current bias
     * @param sig The learning signal
     * @return The updated bias
     */
    private fun handleInterestSignal(currentBias: ProfileBias, sig: LearningSignal): ProfileBias {
        // Increase weights for the tags in the signal
        val updatedWeights = currentBias.tagWeights.toMutableMap()
        
        sig.tags.forEach { tag ->
            val currentWeight = updatedWeights[tag] ?: 0.5f
            updatedWeights[tag] = (currentWeight + 0.2f).coerceAtMost(1.0f)
        }
        
        // Optionally adjust depth comfort if the signal includes depth
        val updatedDepthComfort = if (sig.depth != null && sig.depth > currentBias.depthComfort) {
            minOf(currentBias.depthComfort + 1, 5)
        } else {
            currentBias.depthComfort
        }
        
        // Optionally adjust heat comfort if the signal includes heat
        val updatedHeatComfort = if (sig.heat != null && sig.heat > currentBias.heatComfort) {
            minOf(currentBias.heatComfort + 5, 100)
        } else {
            currentBias.heatComfort
        }
        
        return currentBias.copy(
            tagWeights = updatedWeights,
            depthComfort = updatedDepthComfort,
            heatComfort = updatedHeatComfort
        )
    }
    
    /**
     * Handle a discomfort signal
     * @param currentBias The current bias
     * @param sig The learning signal
     * @return The updated bias
     */
    private fun handleDiscomfortSignal(currentBias: ProfileBias, sig: LearningSignal): ProfileBias {
        // Decrease weights for the tags in the signal
        val updatedWeights = currentBias.tagWeights.toMutableMap()
        
        sig.tags.forEach { tag ->
            val currentWeight = updatedWeights[tag] ?: 0.5f
            updatedWeights[tag] = (currentWeight - 0.2f).coerceAtLeast(0.0f)
        }
        
        // Adjust depth comfort if the signal includes depth
        val updatedDepthComfort = if (sig.depth != null && sig.depth >= currentBias.depthComfort) {
            maxOf(currentBias.depthComfort - 1, 1)
        } else {
            currentBias.depthComfort
        }
        
        // Adjust heat comfort if the signal includes heat
        val updatedHeatComfort = if (sig.heat != null && sig.heat >= currentBias.heatComfort) {
            maxOf(currentBias.heatComfort - 10, 0)
        } else {
            currentBias.heatComfort
        }
        
        return currentBias.copy(
            tagWeights = updatedWeights,
            depthComfort = updatedDepthComfort,
            heatComfort = updatedHeatComfort
        )
    }
    
    /**
     * Handle a skip signal
     * @param currentBias The current bias
     * @param sig The learning signal
     * @return The updated bias
     */
    private fun handleSkipSignal(currentBias: ProfileBias, sig: LearningSignal): ProfileBias {
        // Similar to discomfort but with less impact
        val updatedWeights = currentBias.tagWeights.toMutableMap()
        
        sig.tags.forEach { tag ->
            val currentWeight = updatedWeights[tag] ?: 0.5f
            updatedWeights[tag] = (currentWeight - 0.1f).coerceAtLeast(0.0f)
        }
        
        return currentBias.copy(
            tagWeights = updatedWeights
        )
    }
    
    /**
     * Handle an engagement signal
     * @param currentBias The current bias
     * @param sig The learning signal
     * @return The updated bias
     */
    private fun handleEngagementSignal(currentBias: ProfileBias, sig: LearningSignal): ProfileBias {
        // Similar to interest but with more impact on depth
        val updatedWeights = currentBias.tagWeights.toMutableMap()
        
        sig.tags.forEach { tag ->
            val currentWeight = updatedWeights[tag] ?: 0.5f
            updatedWeights[tag] = (currentWeight + 0.15f).coerceAtMost(1.0f)
        }
        
        // Increase depth comfort more significantly
        val updatedDepthComfort = if (sig.depth != null) {
            minOf(maxOf(currentBias.depthComfort, sig.depth), 5)
        } else {
            minOf(currentBias.depthComfort + 1, 5)
        }
        
        return currentBias.copy(
            tagWeights = updatedWeights,
            depthComfort = updatedDepthComfort
        )
    }
    
    /**
     * Handle a laughter signal
     * @param currentBias The current bias
     * @param sig The learning signal
     * @return The updated bias
     */
    private fun handleLaughterSignal(currentBias: ProfileBias, sig: LearningSignal): ProfileBias {
        // Increase weights for "funny" and related tags
        val updatedWeights = currentBias.tagWeights.toMutableMap()
        
        // Add a boost to funny tag
        updatedWeights["funny"] = (updatedWeights["funny"] ?: 0.5f) + 0.15f
        
        // Also boost the specific tags from the signal
        sig.tags.forEach { tag ->
            val currentWeight = updatedWeights[tag] ?: 0.5f
            updatedWeights[tag] = (currentWeight + 0.1f).coerceAtMost(1.0f)
        }
        
        return currentBias.copy(
            tagWeights = updatedWeights
        )
    }
    
    /**
     * Handle a silence signal
     * @param currentBias The current bias
     * @param sig The learning signal
     * @return The updated bias
     */
    private fun handleSilenceSignal(currentBias: ProfileBias, sig: LearningSignal): ProfileBias {
        // Silence might indicate discomfort with depth or specific topics
        val updatedWeights = currentBias.tagWeights.toMutableMap()
        
        sig.tags.forEach { tag ->
            val currentWeight = updatedWeights[tag] ?: 0.5f
            updatedWeights[tag] = (currentWeight - 0.05f).coerceAtLeast(0.0f)
        }
        
        // Slightly decrease depth comfort if the signal includes depth
        val updatedDepthComfort = if (sig.depth != null && sig.depth >= currentBias.depthComfort) {
            maxOf(currentBias.depthComfort - 1, 1)
        } else {
            currentBias.depthComfort
        }
        
        return currentBias.copy(
            tagWeights = updatedWeights,
            depthComfort = updatedDepthComfort
        )
    }
    
    /**
     * Persist the updated bias to the database
     * @param playerId The player ID
     * @param bias The updated bias
     */
    private suspend fun persistBias(playerId: String, bias: ProfileBias) = withContext(Dispatchers.IO) {
        playerPreferenceRepository.createOrUpdatePreferences(
            playerId = playerId,
            tagWeights = bias.tagWeights,
            depthComfort = bias.depthComfort,
            heatComfort = bias.heatComfort,
            favoriteCategories = emptyList(), // This would be derived from tag weights in a full implementation
            avoidedCategories = emptyList()  // This would be derived from tag weights in a full implementation
        )
    }
}