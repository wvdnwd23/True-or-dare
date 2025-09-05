package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.converters.RoomTypeConverters
import com.wes.truthdare.core.data.dao.PlayerPreferenceDao
import com.wes.truthdare.core.data.entities.PlayerPreferenceEntity
import com.wes.truthdare.core.selector.ProfileBias
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for player preference operations
 */
@Singleton
class PlayerPreferenceRepository @Inject constructor(
    private val playerPreferenceDao: PlayerPreferenceDao,
    private val typeConverters: RoomTypeConverters
) {
    /**
     * Get preferences for a player
     * @param playerId The player ID
     * @return Flow of preferences for the player
     */
    fun getPreferencesForPlayer(playerId: String): Flow<PlayerPreferenceEntity?> {
        return playerPreferenceDao.getPreferencesForPlayer(playerId)
    }
    
    /**
     * Get profile bias for a player
     * @param playerId The player ID
     * @return Flow of profile bias for the player
     */
    fun getProfileBiasForPlayer(playerId: String): Flow<ProfileBias?> {
        return playerPreferenceDao.getPreferencesForPlayer(playerId)
            .map { preferences ->
                preferences?.let {
                    ProfileBias(
                        tagWeights = it.tagWeights,
                        depthComfort = it.depthComfort,
                        heatComfort = it.heatComfort
                    )
                }
            }
    }
    
    /**
     * Get profile bias for a player (suspend function)
     * @param playerId The player ID
     * @return Profile bias for the player, or default if not found
     */
    suspend fun getProfileBiasForPlayerSync(playerId: String): ProfileBias {
        val preferences = playerPreferenceDao.getPreferencesForPlayerSync(playerId)
        return preferences?.let {
            ProfileBias(
                tagWeights = it.tagWeights,
                depthComfort = it.depthComfort,
                heatComfort = it.heatComfort
            )
        } ?: ProfileBias(
            tagWeights = emptyMap(),
            depthComfort = 1,
            heatComfort = 50
        )
    }
    
    /**
     * Create or update preferences for a player
     * @param playerId The player ID
     * @param tagWeights The tag weights
     * @param depthComfort The depth comfort level
     * @param heatComfort The heat comfort level
     * @param favoriteCategories The favorite categories
     * @param avoidedCategories The avoided categories
     */
    suspend fun createOrUpdatePreferences(
        playerId: String,
        tagWeights: Map<String, Float>,
        depthComfort: Int,
        heatComfort: Int,
        favoriteCategories: List<String>,
        avoidedCategories: List<String>
    ) {
        val now = System.currentTimeMillis()
        val existingPreferences = playerPreferenceDao.getPreferencesForPlayerSync(playerId)
        
        if (existingPreferences != null) {
            val updatedPreferences = existingPreferences.copy(
                tagWeights = tagWeights,
                depthComfort = depthComfort,
                heatComfort = heatComfort,
                favoriteCategories = favoriteCategories,
                avoidedCategories = avoidedCategories,
                lastUpdated = now
            )
            playerPreferenceDao.updatePreferences(updatedPreferences)
        } else {
            val newPreferences = PlayerPreferenceEntity(
                id = UUID.randomUUID().toString(),
                playerId = playerId,
                tagWeights = tagWeights,
                depthComfort = depthComfort,
                heatComfort = heatComfort,
                favoriteCategories = favoriteCategories,
                avoidedCategories = avoidedCategories,
                lastUpdated = now
            )
            playerPreferenceDao.insertPreferences(newPreferences)
        }
    }
    
    /**
     * Update tag weights for a player
     * @param playerId The player ID
     * @param tagWeights The tag weights
     */
    suspend fun updateTagWeights(playerId: String, tagWeights: Map<String, Float>) {
        val now = System.currentTimeMillis()
        val tagWeightsJson = typeConverters.fromStringFloatMap(tagWeights)
        playerPreferenceDao.updateTagWeights(playerId, tagWeightsJson, now)
    }
    
    /**
     * Update depth comfort for a player
     * @param playerId The player ID
     * @param depthComfort The depth comfort level
     */
    suspend fun updateDepthComfort(playerId: String, depthComfort: Int) {
        val now = System.currentTimeMillis()
        playerPreferenceDao.updateDepthComfort(playerId, depthComfort, now)
    }
    
    /**
     * Update heat comfort for a player
     * @param playerId The player ID
     * @param heatComfort The heat comfort level
     */
    suspend fun updateHeatComfort(playerId: String, heatComfort: Int) {
        val now = System.currentTimeMillis()
        playerPreferenceDao.updateHeatComfort(playerId, heatComfort, now)
    }
    
    /**
     * Delete preferences for a player
     * @param playerId The player ID
     */
    suspend fun deletePreferencesForPlayer(playerId: String) {
        playerPreferenceDao.deletePreferencesForPlayer(playerId)
    }
    
    /**
     * Delete all player preferences
     */
    suspend fun deleteAllPreferences() {
        playerPreferenceDao.deleteAllPreferences()
    }
}