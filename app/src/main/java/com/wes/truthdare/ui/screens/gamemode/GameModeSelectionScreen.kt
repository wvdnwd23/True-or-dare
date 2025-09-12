package com.wes.truthdare.ui.screens.gamemode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wes.truthdare.R
import com.wes.truthdare.core.data.GameMode
import kotlinx.coroutines.delay

/**
 * Game Mode Selection Screen
 * 
 * This screen allows users to select which game mode they want to play.
 * Each mode is represented by a card with an icon, title, and description.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameModeSelectionScreen(
    onNavigateToClassicMode: (List<String>) -> Unit,
    onNavigateToDrinkMode: () -> Unit,
    onNavigateToPartyMode: (List<String>) -> Unit,
    onNavigateToCouplesMode: (List<String>) -> Unit,
    onNavigateToFamilyMode: (List<String>) -> Unit,
    onNavigateToTeenMode: (List<String>) -> Unit,
    onNavigateToHardcoreMode: (List<String>) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    viewModel: GameModeSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAgeConfirmationDialog by remember { mutableStateOf(false) }
    var selectedGameMode by remember { mutableStateOf<GameMode?>(null) }
    var selectedPlayerIds by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100) // Small delay for smoother animation
        showContent = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_mode_selection_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = stringResource(R.string.statistics)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(500)) + 
                    slideInVertically(animationSpec = tween(500)) { it / 2 },
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_game_mode),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(GameMode.values()) { gameMode ->
                        GameModeCard(
                            gameMode = gameMode,
                            onClick = {
                                if (gameMode.isAgeRestricted()) {
                                    selectedGameMode = gameMode
                                    selectedPlayerIds = uiState.selectedPlayerIds
                                    showAgeConfirmationDialog = true
                                } else {
                                    navigateToGameMode(
                                        gameMode = gameMode,
                                        playerIds = uiState.selectedPlayerIds,
                                        onNavigateToClassicMode = onNavigateToClassicMode,
                                        onNavigateToDrinkMode = onNavigateToDrinkMode,
                                        onNavigateToPartyMode = onNavigateToPartyMode,
                                        onNavigateToCouplesMode = onNavigateToCouplesMode,
                                        onNavigateToFamilyMode = onNavigateToFamilyMode,
                                        onNavigateToTeenMode = onNavigateToTeenMode,
                                        onNavigateToHardcoreMode = onNavigateToHardcoreMode
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Age confirmation dialog for restricted modes
    if (showAgeConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showAgeConfirmationDialog = false },
            title = { Text(stringResource(R.string.age_confirmation_title)) },
            text = { 
                Text(
                    stringResource(
                        R.string.age_confirmation_message,
                        selectedGameMode?.getMinimumAge() ?: 18
                    )
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAgeConfirmationDialog = false
                        selectedGameMode?.let { gameMode ->
                            navigateToGameMode(
                                gameMode = gameMode,
                                playerIds = selectedPlayerIds,
                                onNavigateToClassicMode = onNavigateToClassicMode,
                                onNavigateToDrinkMode = onNavigateToDrinkMode,
                                onNavigateToPartyMode = onNavigateToPartyMode,
                                onNavigateToCouplesMode = onNavigateToCouplesMode,
                                onNavigateToFamilyMode = onNavigateToFamilyMode,
                                onNavigateToTeenMode = onNavigateToTeenMode,
                                onNavigateToHardcoreMode = onNavigateToHardcoreMode
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAgeConfirmationDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Navigate to the selected game mode
 */
private fun navigateToGameMode(
    gameMode: GameMode,
    playerIds: List<String>,
    onNavigateToClassicMode: (List<String>) -> Unit,
    onNavigateToDrinkMode: () -> Unit,
    onNavigateToPartyMode: (List<String>) -> Unit,
    onNavigateToCouplesMode: (List<String>) -> Unit,
    onNavigateToFamilyMode: (List<String>) -> Unit,
    onNavigateToTeenMode: (List<String>) -> Unit,
    onNavigateToHardcoreMode: (List<String>) -> Unit
) {
    when (gameMode) {
        GameMode.CLASSIC -> onNavigateToClassicMode(playerIds)
        GameMode.DRINK -> onNavigateToDrinkMode()
        GameMode.PARTY -> onNavigateToPartyMode(playerIds)
        GameMode.COUPLES -> onNavigateToCouplesMode(playerIds)
        GameMode.FAMILY -> onNavigateToFamilyMode(playerIds)
        GameMode.TEEN -> onNavigateToTeenMode(playerIds)
        GameMode.HARDCORE -> onNavigateToHardcoreMode(playerIds)
    }
}

/**
 * Game Mode Card component
 */
@Composable
fun GameModeCard(
    gameMode: GameMode,
    onClick: () -> Unit
) {
    val (icon, iconTint) = when (gameMode) {
        GameMode.CLASSIC -> Icons.Default.EmojiEvents to MaterialTheme.colorScheme.primary
        GameMode.DRINK -> Icons.Default.LocalBar to Color(0xFF8D6E63)
        GameMode.PARTY -> Icons.Default.Celebration to Color(0xFFFF9800)
        GameMode.COUPLES -> Icons.Default.Favorite to Color(0xFFE91E63)
        GameMode.FAMILY -> Icons.Default.FamilyRestroom to Color(0xFF4CAF50)
        GameMode.TEEN -> Icons.Default.Groups to Color(0xFF2196F3)
        GameMode.HARDCORE -> Icons.Default.Whatshot to Color(0xFFF44336)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(
                    when (gameMode) {
                        GameMode.CLASSIC -> R.string.classic_mode
                        GameMode.DRINK -> R.string.drink_mode
                        GameMode.PARTY -> R.string.party_mode
                        GameMode.COUPLES -> R.string.couples_mode
                        GameMode.FAMILY -> R.string.family_mode
                        GameMode.TEEN -> R.string.teen_mode
                        GameMode.HARDCORE -> R.string.hardcore_mode
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = gameMode.getDescription(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 3
            )
            
            if (gameMode.isAgeRestricted()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = stringResource(R.string.age_restricted, gameMode.getMinimumAge()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}