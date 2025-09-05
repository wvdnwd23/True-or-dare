package com.wes.truthdare.core.agents

import com.wes.truthdare.core.data.repositories.PlayerPreferenceRepository
import com.wes.truthdare.core.impl.DefaultLearningAgent
import com.wes.truthdare.core.selector.ProfileBias
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class LearningAgentTest {

    private lateinit var learningAgent: DefaultLearningAgent
    private lateinit var playerPreferenceRepository: PlayerPreferenceRepository

    @Before
    fun setup() {
        playerPreferenceRepository = mock(PlayerPreferenceRepository::class.java)
        learningAgent = DefaultLearningAgent(playerPreferenceRepository)
    }

    @Test
    fun `test interest signal increases tag weights`() = runBlocking {
        // Given
        val playerId = "player1"
        val initialBias = ProfileBias(
            tagWeights = mapOf("reizen" to 1.0f, "sport" to 1.0f),
            depthComfort = 2,
            heatComfort = 50
        )

        `when`(playerPreferenceRepository.getProfileBiasForPlayerSync(playerId))
            .thenReturn(initialBias)

        val signal = LearningSignal(
            playerId = playerId,
            type = SignalType.INTEREST,
            questionId = "q1",
            tags = listOf("reizen", "vakantie"),
            heat = 60,
            depth = 2
        )

        // When
        learningAgent.updateSignals(signal)

        // Then
        // Verify that the repository was called to update the bias
        // In a real test, we would capture the argument and verify its values
        verify(playerPreferenceRepository).updateProfileBias(playerId, initialBias.copy(
            tagWeights = mapOf(
                "reizen" to 1.2f,  // Increased weight
                "sport" to 1.0f,    // Unchanged
                "vakantie" to 1.1f  // New tag
            )
        ))
    }

    @Test
    fun `test skip signal decreases tag weights`() = runBlocking {
        // Given
        val playerId = "player1"
        val initialBias = ProfileBias(
            tagWeights = mapOf("persoonlijk" to 1.0f, "diep" to 1.0f),
            depthComfort = 3,
            heatComfort = 60
        )

        `when`(playerPreferenceRepository.getProfileBiasForPlayerSync(playerId))
            .thenReturn(initialBias)

        val signal = LearningSignal(
            playerId = playerId,
            type = SignalType.SKIP,
            questionId = "q2",
            tags = listOf("persoonlijk", "intiem"),
            heat = 70,
            depth = 4
        )

        // When
        learningAgent.updateSignals(signal)

        // Then
        // Verify that the repository was called to update the bias
        // In a real test, we would capture the argument and verify its values
        verify(playerPreferenceRepository).updateProfileBias(playerId, initialBias.copy(
            tagWeights = mapOf(
                "persoonlijk" to 0.9f,  // Decreased weight
                "diep" to 1.0f,         // Unchanged
                "intiem" to 0.9f        // New tag with decreased weight
            ),
            depthComfort = 2,  // Decreased because depth > comfort
            heatComfort = 55   // Decreased because heat > comfort
        ))
    }

    @Test
    fun `test discomfort signal significantly decreases weights`() = runBlocking {
        // Given
        val playerId = "player1"
        val initialBias = ProfileBias(
            tagWeights = mapOf("uitdaging" to 1.0f, "feest" to 1.0f),
            depthComfort = 2,
            heatComfort = 70
        )

        `when`(playerPreferenceRepository.getProfileBiasForPlayerSync(playerId))
            .thenReturn(initialBias)

        val signal = LearningSignal(
            playerId = playerId,
            type = SignalType.DISCOMFORT,
            questionId = "q3",
            tags = listOf("uitdaging", "gênant"),
            heat = 80,
            depth = 1
        )

        // When
        learningAgent.updateSignals(signal)

        // Then
        // Verify that the repository was called to update the bias
        // In a real test, we would capture the argument and verify its values
        verify(playerPreferenceRepository).updateProfileBias(playerId, initialBias.copy(
            tagWeights = mapOf(
                "uitdaging" to 0.7f,  // Significantly decreased weight
                "feest" to 1.0f,      // Unchanged
                "gênant" to 0.7f      // New tag with decreased weight
            ),
            heatComfort = 60   // Significantly decreased because of discomfort
        ))
    }

    @Test
    fun `test currentBias returns repository value`() = runBlocking {
        // Given
        val playerId = "player1"
        val expectedBias = ProfileBias(
            tagWeights = mapOf("humor" to 1.2f, "grappig" to 1.1f),
            depthComfort = 2,
            heatComfort = 55
        )

        `when`(playerPreferenceRepository.getProfileBiasForPlayerSync(playerId))
            .thenReturn(expectedBias)

        // When
        val result = learningAgent.currentBias(playerId)

        // Then
        assertEquals(expectedBias, result)
    }

    @Test
    fun `test default bias for new player`() = runBlocking {
        // Given
        val playerId = "newPlayer"
        val defaultBias = ProfileBias(
            tagWeights = emptyMap(),
            depthComfort = 1,
            heatComfort = 50
        )

        `when`(playerPreferenceRepository.getProfileBiasForPlayerSync(playerId))
            .thenReturn(defaultBias)

        // When
        val result = learningAgent.currentBias(playerId)

        // Then
        assertEquals(defaultBias, result)
        assertTrue(result.tagWeights.isEmpty())
        assertEquals(1, result.depthComfort)
        assertEquals(50, result.heatComfort)
    }
}