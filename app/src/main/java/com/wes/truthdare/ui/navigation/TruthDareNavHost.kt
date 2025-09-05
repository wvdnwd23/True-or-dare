package com.wes.truthdare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.ui.screens.categories.CategoriesScreen
import com.wes.truthdare.ui.screens.categories.CategoriesViewModel
import com.wes.truthdare.ui.screens.game.GameScreen
import com.wes.truthdare.ui.screens.game.GameViewModel
import com.wes.truthdare.ui.screens.onboarding.OnboardingScreen
import com.wes.truthdare.ui.screens.onboarding.OnboardingViewModel
import com.wes.truthdare.ui.screens.players.PlayersScreen
import com.wes.truthdare.ui.screens.players.PlayersViewModel
import com.wes.truthdare.ui.screens.reflect.ReflectScreen
import com.wes.truthdare.ui.screens.reflect.ReflectViewModel
import com.wes.truthdare.ui.screens.settings.SettingsScreen
import com.wes.truthdare.ui.screens.settings.SettingsViewModel

/**
 * Main navigation host for the app
 */
@Composable
fun TruthDareNavHost(
    navController: NavHostController = rememberNavController(),
    appPreferences: AppPreferences = hiltViewModel<AppPreferencesViewModel>().appPreferences,
    startDestination: String = NavRoutes.ONBOARDING
) {
    // Check if onboarding is completed
    val onboardingCompleted by appPreferences.onboardingCompleted.collectAsState(initial = false)
    
    // Determine the start destination based on onboarding status
    val actualStartDestination = if (onboardingCompleted) {
        NavRoutes.PLAYERS
    } else {
        NavRoutes.ONBOARDING
    }
    
    NavHost(
        navController = navController,
        startDestination = actualStartDestination
    ) {
        // Onboarding screen
        composable(NavRoutes.ONBOARDING) {
            val viewModel = hiltViewModel<OnboardingViewModel>()
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(NavRoutes.PLAYERS) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        
        // Players screen
        composable(NavRoutes.PLAYERS) {
            val viewModel = hiltViewModel<PlayersViewModel>()
            PlayersScreen(
                viewModel = viewModel,
                onNavigateToCategories = { playerIds ->
                    navController.navigate("${NavRoutes.CATEGORIES}/${playerIds.joinToString(",")}")
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }
        
        // Categories screen
        composable(
            route = "${NavRoutes.CATEGORIES}/{playerIds}",
            arguments = listOf(
                navArgument("playerIds") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val playerIds = backStackEntry.arguments?.getString("playerIds")?.split(",") ?: emptyList()
            val viewModel = hiltViewModel<CategoriesViewModel>()
            CategoriesScreen(
                viewModel = viewModel,
                playerIds = playerIds,
                onNavigateToGame = { sessionId ->
                    navController.navigate("${NavRoutes.GAME}/$sessionId") {
                        popUpTo(NavRoutes.CATEGORIES) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Game screen
        composable(
            route = "${NavRoutes.GAME}/{sessionId}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val viewModel = hiltViewModel<GameViewModel>()
            GameScreen(
                viewModel = viewModel,
                sessionId = sessionId,
                onEndGame = { sessionId ->
                    navController.navigate("${NavRoutes.REFLECT}/$sessionId") {
                        popUpTo(NavRoutes.GAME) { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }
        
        // Settings screen
        composable(NavRoutes.SETTINGS) {
            val viewModel = hiltViewModel<SettingsViewModel>()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayers = {
                    navController.navigate(NavRoutes.PLAYERS) {
                        popUpTo(NavRoutes.SETTINGS) { inclusive = true }
                    }
                }
            )
        }
        
        // Reflect screen
        composable(
            route = "${NavRoutes.REFLECT}/{sessionId}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val viewModel = hiltViewModel<ReflectViewModel>()
            ReflectScreen(
                viewModel = viewModel,
                sessionId = sessionId,
                onNavigateToPlayers = {
                    navController.navigate(NavRoutes.PLAYERS) {
                        popUpTo(NavRoutes.REFLECT) { inclusive = true }
                    }
                },
                onStartNewGame = { playerIds ->
                    navController.navigate("${NavRoutes.CATEGORIES}/${playerIds.joinToString(",")}") {
                        popUpTo(NavRoutes.REFLECT) { inclusive = true }
                    }
                }
            )
        }
    }
}