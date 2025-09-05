package com.wes.truthdare.core.selector

import com.wes.truthdare.core.agents.Mood
import com.wes.truthdare.core.agents.Question
import com.wes.truthdare.core.agents.QuestionAgent
import com.wes.truthdare.core.agents.SafetyAgent
import com.wes.truthdare.core.agents.SafetyDecision
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.nlp.NlpResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class QuestionSelectorTest {

    private lateinit var questionSelector: QuestionSelector
    private lateinit var questionAgent: QuestionAgent
    private lateinit var safetyAgent: SafetyAgent
    private lateinit var nlpEngine: NlpEngine

    @Before
    fun setup() {
        questionAgent = mock(QuestionAgent::class.java)
        safetyAgent = mock(SafetyAgent::class.java)
        nlpEngine = mock(NlpEngine::class.java)
        questionSelector = QuestionSelector(questionAgent, safetyAgent, nlpEngine)
    }

    @Test
    fun `test star queue prioritization`() = runBlocking {
        // Given
        val starTag = "romantisch"
        val context = SelectorContext(
            playerId = "player1",
            mode = GameMode.CASUAL,
            heat = 50,
            maxDepth = 3,
            starTagsQueue = listOf(starTag),
            lastTags = emptyList(),
            bias = ProfileBias(emptyMap(), 3, 50),
            mood = Mood.CALM
        )

        val question = Question(
            id = "q1",
            type = "truth",
            category = "romantic",
            targets = "single",
            depthLevel = 2,
            tags = listOf("romantisch", "relatie"),
            text = "Wat is je meest romantische herinnering?"
        )

        `when`(questionAgent.nextQuestion(context.copy(lastTags = listOf(starTag) + context.lastTags)))
            .thenReturn(question)
        `when`(safetyAgent.check(question, context))
            .thenReturn(SafetyDecision(true, null))

        // When
        val result = questionSelector.selectNext(context)

        // Then
        assertEquals(question, result)
    }

    @Test
    fun `test follow-up question based on answer`() = runBlocking {
        // Given
        val answer = "Ik vind het leuk om te reizen naar warme landen."
        val context = SelectorContext(
            playerId = "player1",
            mode = GameMode.CASUAL,
            heat = 50,
            maxDepth = 3,
            starTagsQueue = emptyList(),
            lastTags = listOf("reizen", "vakantie"),
            bias = ProfileBias(emptyMap(), 3, 50),
            mood = Mood.HAPPY
        )

        val followUpQuestion = Question(
            id = "q2",
            type = "truth",
            category = "casual",
            targets = "single",
            depthLevel = 1,
            tags = listOf("reizen", "zomer"),
            text = "Wat is je favoriete reisbestemming?"
        )

        `when`(nlpEngine.analyze(answer))
            .thenReturn(NlpResult(
                tags = listOf("reizen", "warm", "landen"),
                intent = "preference",
                sentiment = 70,
                triggered = false
            ))

        `when`(questionAgent.followUpFor(answer, context))
            .thenReturn(followUpQuestion)

        `when`(safetyAgent.check(followUpQuestion, context))
            .thenReturn(SafetyDecision(true, null))

        // When
        val result = questionSelector.maybeAskFollowUp(answer, context)

        // Then
        assertNotNull(result)
        assertEquals(followUpQuestion, result)
    }

    @Test
    fun `test no follow-up when no relevant tags`() = runBlocking {
        // Given
        val answer = "Ik weet het niet."
        val context = SelectorContext(
            playerId = "player1",
            mode = GameMode.CASUAL,
            heat = 50,
            maxDepth = 3,
            starTagsQueue = emptyList(),
            lastTags = listOf("reizen", "vakantie"),
            bias = ProfileBias(emptyMap(), 3, 50),
            mood = Mood.CALM
        )

        `when`(nlpEngine.analyze(answer))
            .thenReturn(NlpResult(
                tags = listOf("onzeker"),
                intent = null,
                sentiment = 0,
                triggered = false
            ))

        // When
        val result = questionSelector.maybeAskFollowUp(answer, context)

        // Then
        assertNull(result)
    }

    @Test
    fun `test no follow-up when triggered content detected`() = runBlocking {
        // Given
        val answer = "Ik vind het *** (gevoelige inhoud)"
        val context = SelectorContext(
            playerId = "player1",
            mode = GameMode.CASUAL,
            heat = 50,
            maxDepth = 3,
            starTagsQueue = emptyList(),
            lastTags = listOf("mening"),
            bias = ProfileBias(emptyMap(), 3, 50),
            mood = Mood.SERIOUS
        )

        `when`(nlpEngine.analyze(answer))
            .thenReturn(NlpResult(
                tags = listOf("mening"),
                intent = "opinion",
                sentiment = -20,
                triggered = true
            ))

        // When
        val result = questionSelector.maybeAskFollowUp(answer, context)

        // Then
        assertNull(result)
    }
}