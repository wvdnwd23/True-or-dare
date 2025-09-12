package com.wes.truthdare.ui.screens.drink

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wes.truthdare.R
import com.wes.truthdare.core.util.WaterReminderManager
import com.wes.truthdare.ui.components.*

/**
 * Drink Mode Screen for Truth or Dare Deluxe
 * 
 * This screen provides a drinking game variant with:
 * - Shot counter
 * - Lie card feature
 * - Mini-games
 * - Special drinking rules
 * - Responsible drinking features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkModeScreen(
    onNavigateBack: () -> Unit,
    viewModel: DrinkModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val waterReminderState by viewModel.waterReminderState.collectAsState()
    
    // Show dialogs based on UI state
    if (uiState.showDifficultyFilterDialog) {
        DifficultyFilterDialog(
            selectedDifficulty = uiState.selectedDifficulty,
            onDifficultySelected = { difficulty ->
                viewModel.setDifficultyFilter(difficulty)
                viewModel.hideDifficultyFilterDialog()
            },
            onDismiss = { viewModel.hideDifficultyFilterDialog() }
        )
    }
    
    if (uiState.showGameInstructionsDialog && uiState.currentMiniGame != null) {
        GameInstructionsDialog(
            miniGame = uiState.currentMiniGame!!,
            onDismiss = { viewModel.hideGameInstructionsDialog() }
        )
    }
    val designatedDriverId by viewModel.designatedDriverId.collectAsState(initial = null)
    val timeLimitMinutes by viewModel.timeLimitMinutes.collectAsState(initial = null)
    val drinkLimitCount by viewModel.drinkLimitCount.collectAsState(initial = null)
    val remainingTimeMinutes by viewModel.remainingTimeMinutes.collectAsState(initial = null)
    val isTimeLimitReached by viewModel.isTimeLimitReached.collectAsState(initial = false)
    
    // Dialog states
    var showWaterReminderDialog by remember { mutableStateOf(false) }
    var showDesignatedDriverDialog by remember { mutableStateOf(false) }
    var showSessionLimitsDialog by remember { mutableStateOf(false) }
    var showEmergencyContactsDialog by remember { mutableStateOf(false) }
    
    // Check if water reminder should be shown
    LaunchedEffect(waterReminderState) {
        if (waterReminderState is WaterReminderManager.ReminderState.ShowReminder) {
            showWaterReminderDialog = true
        }
    }
    
    // Check if time limit has been reached
    LaunchedEffect(isTimeLimitReached) {
        if (isTimeLimitReached && timeLimitMinutes != null) {
            // Show alert that time limit has been reached
            showWaterReminderDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.drink_mode_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Reset shot counter
                    IconButton(onClick = { viewModel.resetShotCounter() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Shot Counter"
                        )
                    }
                    
                    // Responsible drinking menu
                    var showMenu by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.water_reminder_title)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showWaterReminderDialog = true
                                showMenu = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.designated_driver)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showDesignatedDriverDialog = true
                                showMenu = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.session_limits)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showSessionLimitsDialog = true
                                showMenu = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.emergency_contacts)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showEmergencyContactsDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Responsible Drinking Banner
            if (remainingTimeMinutes != null || drinkLimitCount != null || designatedDriverId != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.drink_responsibly),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show active limits
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (remainingTimeMinutes != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "$remainingTimeMinutes min",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            if (drinkLimitCount != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LocalBar,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "$drinkLimitCount max",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            if (designatedDriverId != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Driver set",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Shot Counter
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.drink_mode_shot_counter),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AnimatedCounter(
                        count = uiState.shotCount,
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.decrementShotCounter() },
                            enabled = uiState.shotCount > 0
                        ) {
                            Text("-")
                        }
                        
                        Button(
                            onClick = { viewModel.incrementShotCounter() }
                        ) {
                            Text("+")
                        }
                    }
                }
            }
            
            // Lie Card Feature
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.drink_mode_lie_cards),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Remaining: ${uiState.remainingLieCards}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.useLieCard() },
                        enabled = uiState.remainingLieCards > 0
                    ) {
                        Text("Use Lie Card")
                    }
                }
            }
            
            // Mini-Game Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.drink_mode_mini_games),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.generateRandomMiniGame() }
                        ) {
                            Text("Random Mini-Game")
                        }
                        
                        IconButton(
                            onClick = { viewModel.showDifficultyFilterDialog() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter by Difficulty"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.currentMiniGame != null) {
                        GameCard(
                            title = uiState.currentMiniGame!!.title,
                            description = uiState.currentMiniGame!!.description,
                            difficulty = uiState.currentMiniGame!!.difficulty,
                            onViewInstructions = { viewModel.showGameInstructionsDialog() }
                        )
                    } else {
                        Text(
                            text = "Tap the button to generate a random mini-game",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Drinking Rules
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.drink_mode_rules),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(uiState.drinkingRules) { rule ->
                            ListItem(
                                headlineContent = { Text(rule.title) },
                                supportingContent = { Text(rule.description) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
    
    // Water Reminder Dialog
    if (showWaterReminderDialog) {
        WaterReminderDialog(
            onDismiss = {
                showWaterReminderDialog = false
                viewModel.dismissWaterReminder()
            },
            onDismissAndRemind = {
                showWaterReminderDialog = false
                viewModel.dismissWaterReminderAndRemindLater()
            }
        )
    }
    
    // Designated Driver Dialog
    if (showDesignatedDriverDialog) {
        // This would need to be implemented with player data
        // For now, we'll just show a placeholder dialog
        AlertDialog(
            onDismissRequest = { showDesignatedDriverDialog = false },
            title = { Text(stringResource(R.string.designated_driver)) },
            text = { Text("Select a designated driver from your player list.") },
            confirmButton = {
                Button(onClick = { showDesignatedDriverDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Session Limits Dialog
    if (showSessionLimitsDialog) {
        SessionLimitsDialog(
            initialTimeLimit = timeLimitMinutes,
            initialDrinkLimit = drinkLimitCount,
            onSetLimits = { timeLimit, drinkLimit ->
                viewModel.setTimeLimit(timeLimit)
                viewModel.setDrinkLimit(drinkLimit)
            },
            onDismiss = { showSessionLimitsDialog = false }
        )
    }
    
    // Emergency Contacts Dialog
    if (showEmergencyContactsDialog) {
        EmergencyContactsDialog(
            onDismiss = { showEmergencyContactsDialog = false }
        )
    }
}