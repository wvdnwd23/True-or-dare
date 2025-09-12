package com.wes.truthdare.ui.screens.drink

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class DrinkModeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DrinkModeViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DrinkModeViewModel()
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
}