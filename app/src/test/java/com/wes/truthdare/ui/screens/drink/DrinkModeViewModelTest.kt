package com.wes.truthdare.ui.screens.drink

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wes.truthdare.core.data.DesignatedDriverManager
import com.wes.truthdare.core.data.SessionLimitsManager
import com.wes.truthdare.core.util.WaterReminderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class DrinkModeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var waterReminderManager: WaterReminderManager
    
    @Mock
    private lateinit var designatedDriverManager: DesignatedDriverManager
    
    @Mock
    private lateinit var sessionLimitsManager: SessionLimitsManager
    
    private lateinit var viewModel: DrinkModeViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup mock behavior
        `when`(waterReminderManager.reminderState).thenReturn(MutableStateFlow(WaterReminderManager.ReminderState.Idle))
        `when`(designatedDriverManager.designatedDriverId).thenReturn(MutableStateFlow(null))
        `when`(sessionLimitsManager.timeLimitMinutes).thenReturn(MutableStateFlow(null))
        `when`(sessionLimitsManager.drinkLimitCount).thenReturn(MutableStateFlow(null))
        `when`(sessionLimitsManager.getRemainingTimeMinutes()).thenReturn(MutableStateFlow(null))
        `when`(sessionLimitsManager.isTimeLimitReached()).thenReturn(MutableStateFlow(false))
        
        viewModel = DrinkModeViewModel(
            waterReminderManager = waterReminderManager,
            designatedDriverManager = designatedDriverManager,
            sessionLimitsManager = sessionLimitsManager
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state has correct values`() = runTest {
        val initialState = viewModel.uiState.value
        
        assertEquals(0, initialState.shotCount)
        assertEquals(DrinkModeViewModel.DEFAULT_LIE_CARDS, initialState.remainingLieCards)
        assertTrue(initialState.drinkingRules.isNotEmpty())
        assertTrue(initialState.miniGames.isNotEmpty())
        assertNull(initialState.currentMiniGame)
    }
    
    @Test
    fun `incrementShotCounter increases shot count`() = runTest {
        val initialCount = viewModel.uiState.value.shotCount
        
        viewModel.incrementShotCounter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(initialCount + 1, viewModel.uiState.value.shotCount)
    }
    
    @Test
    fun `decrementShotCounter decreases shot count`() = runTest {
        // First increment to ensure we have a positive count
        viewModel.incrementShotCounter()
        viewModel.incrementShotCounter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val countBeforeDecrement = viewModel.uiState.value.shotCount
        
        viewModel.decrementShotCounter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(countBeforeDecrement - 1, viewModel.uiState.value.shotCount)
    }
    
    @Test
    fun `decrementShotCounter does not go below zero`() = runTest {
        // Ensure we start at zero
        viewModel.resetShotCounter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.decrementShotCounter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(0, viewModel.uiState.value.shotCount)
    }
    
    @Test
    fun `resetShotCounter sets count to zero`() = runTest {
        // First increment to ensure we have a positive count
        viewModel.incrementShotCounter()
        viewModel.incrementShotCounter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.resetShotCounter()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(0, viewModel.uiState.value.shotCount)
    }
    
    @Test
    fun `useLieCard decreases remaining lie cards`() = runTest {
        val initialLieCards = viewModel.uiState.value.remainingLieCards
        
        viewModel.useLieCard()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(initialLieCards - 1, viewModel.uiState.value.remainingLieCards)
    }
    
    @Test
    fun `useLieCard does not go below zero`() = runTest {
        // Use all lie cards
        repeat(DrinkModeViewModel.DEFAULT_LIE_CARDS + 1) {
            viewModel.useLieCard()
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(0, viewModel.uiState.value.remainingLieCards)
    }
    
    @Test
    fun `resetLieCards sets cards to default amount`() = runTest {
        // Use some lie cards first
        viewModel.useLieCard()
        viewModel.useLieCard()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.resetLieCards()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(DrinkModeViewModel.DEFAULT_LIE_CARDS, viewModel.uiState.value.remainingLieCards)
    }
    
    @Test
    fun `generateRandomMiniGame sets a mini-game from the list`() = runTest {
        assertNull(viewModel.uiState.value.currentMiniGame)
        
        viewModel.generateRandomMiniGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertNotNull(viewModel.uiState.value.currentMiniGame)
        assertTrue(viewModel.uiState.value.miniGames.contains(viewModel.uiState.value.currentMiniGame))
    }
    
    @Test
    fun `mini-games list contains 100 games`() = runTest {
        assertEquals(100, viewModel.uiState.value.miniGames.size)
    }
    
    @Test
    fun `mini-games have appropriate difficulty distribution`() = runTest {
        val miniGames = viewModel.uiState.value.miniGames
        
        val easyGames = miniGames.filter { it.difficulty == Difficulty.EASY }
        val mediumGames = miniGames.filter { it.difficulty == Difficulty.MEDIUM }
        val hardGames = miniGames.filter { it.difficulty == Difficulty.HARD }
        
        // Check that we have a roughly equal distribution
        assertTrue(easyGames.size >= 30)
        assertTrue(mediumGames.size >= 30)
        assertTrue(hardGames.size >= 30)
        
        // Check that all difficulties add up to 100
        assertEquals(100, easyGames.size + mediumGames.size + hardGames.size)
    }
    
    @Test
    fun `setDifficultyFilter correctly filters mini-games`() = runTest {
        // Test filtering by EASY
        viewModel.setDifficultyFilter(Difficulty.EASY)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val easyGames = viewModel.uiState.value.filteredMiniGames
        assertTrue(easyGames.isNotEmpty())
        assertTrue(easyGames.all { it.difficulty == Difficulty.EASY })
        
        // Test filtering by MEDIUM
        viewModel.setDifficultyFilter(Difficulty.MEDIUM)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val mediumGames = viewModel.uiState.value.filteredMiniGames
        assertTrue(mediumGames.isNotEmpty())
        assertTrue(mediumGames.all { it.difficulty == Difficulty.MEDIUM })
        
        // Test filtering by HARD
        viewModel.setDifficultyFilter(Difficulty.HARD)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val hardGames = viewModel.uiState.value.filteredMiniGames
        assertTrue(hardGames.isNotEmpty())
        assertTrue(hardGames.all { it.difficulty == Difficulty.HARD })
        
        // Test clearing filter (null should show all games)
        viewModel.setDifficultyFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(viewModel.uiState.value.miniGames, viewModel.uiState.value.filteredMiniGames)
    }
    
    @Test
    fun `generateRandomFilteredMiniGame respects current filter`() = runTest {
        // Set filter to EASY
        viewModel.setDifficultyFilter(Difficulty.EASY)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Generate random game
        viewModel.generateRandomFilteredMiniGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Check that the selected game is EASY
        assertNotNull(viewModel.uiState.value.currentMiniGame)
        assertEquals(Difficulty.EASY, viewModel.uiState.value.currentMiniGame?.difficulty)
        
        // Set filter to MEDIUM
        viewModel.setDifficultyFilter(Difficulty.MEDIUM)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Generate random game
        viewModel.generateRandomFilteredMiniGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Check that the selected game is MEDIUM
        assertNotNull(viewModel.uiState.value.currentMiniGame)
        assertEquals(Difficulty.MEDIUM, viewModel.uiState.value.currentMiniGame?.difficulty)
        
        // Set filter to HARD
        viewModel.setDifficultyFilter(Difficulty.HARD)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Generate random game
        viewModel.generateRandomFilteredMiniGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Check that the selected game is HARD
        assertNotNull(viewModel.uiState.value.currentMiniGame)
        assertEquals(Difficulty.HARD, viewModel.uiState.value.currentMiniGame?.difficulty)
    }
    
    @Test
    fun `mini-games have non-empty descriptions`() = runTest {
        val miniGames = viewModel.uiState.value.miniGames
        
        for (game in miniGames) {
            assertTrue(game.title.isNotEmpty())
            assertTrue(game.description.isNotEmpty())
        }
    }
}