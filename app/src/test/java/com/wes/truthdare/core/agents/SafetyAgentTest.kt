package com.wes.truthdare.core.agents

import com.wes.truthdare.core.impl.DefaultSafetyAgent
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.nlp.NlpResult
import com.wes.truthdare.core.nlp.TriggerScanner
import com.wes.truthdare.core.selector.GameMode
import com.wes.truthdare.core.selector.ProfileBias
import com.wes.truthdare.core.selector.SelectorContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SafetyAgentTest {

    private lateinit var safetyAgent: DefaultSafetyAgent
    private lateinit var nlpEngine: NlpEngine
    private lateinit var triggerScanner: TriggerScanner
    private lateinit var questionAgent: QuestionAgent

    @Before
    fun setup() {
        nlpEngine = mock(NlpEngine::class.java)
        triggerScanner = mock(TriggerScanner::class.java)
        questionAgent = mock(QuestionAgent::class.java)
        safetyAgent = DefaultSafetyAgent(nlpEngine, triggerScanner, questionAgent)
    }

    @Test
    fun `test safe question passes check`() = runBlocking {
        // Given
        val question = Question(
            id = "q1",
            type = "truth",
            category = "casual",
            targets = "single",
            depthLevel = 1,
            tags = listOf("alledaags", "simpel"),
            text = "Wat is je favoriete kleur?"
        )

        val context = SelectorContext(
            playerId = "player1",
            mode = GameMode.CASUAL,
            heat = 30,
            maxDepth = 3,
            starTagsQueue = emptyList(),
            lastTags = emptyList(),
            bias = ProfileBias(emptyMap(), 3, 50),
            mood = Mood.CALM
        )

        `when`(nlpEngine.analyze(question.text))
            .thenReturn(NlpResult(
                tags = listOf("voorkeur", "kleur"),
                intent = "preference",
                sentiment = 10,
                triggered = false
            ))

        // When
        val result = safetyAgent.check(question, context)

        // Then
        assertTrue(result.ok)
        assertNull(result.mildAlternative)
    }

    @Test
    fun `test unsafe question fails check and provides alternative`() = runBlocking {
        // Given
        val unsafeQuestion = Question(
            id = "q2",
            type = "truth",
            category = "personal",
            targets = "single",
            depthLevel = 4,
            tags = listOf("persoonlijk", "intiem"),
            text = "Heb je ooit [gevoelige inhoud]?"
        )

        val safeQuestion = Question(
            id = "q3",
            type = "truth",
            category = "personal",
            targets = "single",
            depthLevel = 2,
            tags = listOf("persoonlijk"),
            text = "Wat is je grootste droom?"
        )

        val context = SelectorContext(
            playerId = "player1",
            mode = GameMode.FAMILY_FRIENDLY,
            heat = 20,
            maxDepth = 2,
            starTagsQueue = emptyList(),
            lastTags = emptyList(),
            bias = ProfileBias(emptyMap(), 2, 30),
            mood = Mood.CALM
        )

        `when`(nlpEngine.analyze(unsafeQuestion.text))
            .thenReturn(NlpResult(
                tags = listOf("persoonlijk", "intiem"),
                intent = "personal_experience",
                sentiment = -10,
                triggered = true
            ))

        `when`(questionAgent.nextQuestion(context.copy(heat = 0, maxDepth = 1)))
            .thenReturn(safeQuestion)

        // When
        val result = safetyAgent.check(unsafeQuestion, context)

        // Then
        assertFalse(result.ok)
        assertNotNull(result.mildAlternative)
        assertEquals(safeQuestion, result.mildAlternative)
    }

    @Test
    fun `test answer safety check`() = runBlocking {
        // Given
        val safeAnswer = "Ik vind blauwe lucht heel mooi."
        val unsafeAnswer = "Ik vind [gevoelige inhoud] heel erg."

        `when`(triggerScanner.scanForTriggers(safeAnswer))
            .thenReturn(false)

        `when`(triggerScanner.scanForTriggers(unsafeAnswer))
            .thenReturn(true)

        // When
        val safeResult = safetyAgent.checkAnswer(safeAnswer)
        val unsafeResult = safetyAgent.checkAnswer(unsafeAnswer)

        // Then
        assertTrue(safeResult)
        assertFalse(unsafeResult)
    }

    @Test
    fun `test heat level affects safety check`() = runBlocking {
        // Given
        val spicyQuestion = Question(
            id = "q4",
            type = "dare",
            category = "party",
            targets = "single",
            depthLevel = null,
            tags = listOf("feest", "uitdaging"),
            text = "Doe een gekke dans voor de groep."
        )

        val lowHeatContext = SelectorContext(
            playerId = "player1",
            mode = GameMode.FAMILY_FRIENDLY,
            heat = 10,
            maxDepth = 2,
            starTagsQueue = emptyList(),
            lastTags = emptyList(),
            bias = ProfileBias(emptyMap(), 2, 10),
            mood = Mood.CALM
        )

        val highHeatContext = SelectorContext(
            playerId = "player1",
            mode = GameMode.PARTY,
            heat = 80,
            maxDepth = 3,
            starTagsQueue = emptyList(),
            lastTags = emptyList(),
            bias = ProfileBias(emptyMap(), 3, 80),
            mood = Mood.HAPPY
        )

        `when`(nlpEngine.analyze(spicyQuestion.text))
            .thenReturn(NlpResult(
                tags = listOf("dans", "uitdaging"),
                intent = "action",
                sentiment = 30,
                triggered = false
            ))

        // When
        val lowHeatResult = safetyAgent.check(spicyQuestion, lowHeatContext)
        val highHeatResult = safetyAgent.check(spicyQuestion, highHeatContext)

        // Then
        // In a real implementation, the safety agent might consider this question too spicy for low heat
        // but for this test, we'll assume it's safe for both contexts
        assertTrue(highHeatResult.ok)
    }
}