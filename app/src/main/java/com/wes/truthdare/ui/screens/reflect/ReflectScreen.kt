package com.wes.truthdare.ui.screens.reflect

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wes.truthdare.R
import com.wes.truthdare.core.agents.Mood
import com.wes.truthdare.core.data.entities.PlayerEntity

/**
 * Reflect screen for session summary
 */
@Composable
fun ReflectScreen(
    viewModel: ReflectViewModel,
    sessionId: String,
    onNavigateToPlayers: () -> Unit,
    onStartNewGame: (List<String>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load session when the screen is first displayed
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }
    
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading indicator while loading
            if (uiState.isLoading) {
                LoadingView()
            } else {
                // Show summary content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Text(
                        text = stringResource(R.string.reflect_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.reflect_summary),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Players section
                    PlayersSection(players = uiState.players)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Top tags section
                    TopTagsSection(tags = uiState.summary?.topTags ?: emptyList())
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Highlights section
                    HighlightsSection(
                        playerHighlights = uiState.summary?.playerHighlights ?: emptyMap(),
                        players = uiState.players
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Special moments section
                    SpecialMomentsSection(
                        deepestMoment = uiState.summary?.deepestMoment,
                        funniestMoment = uiState.summary?.funniestMoment
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mood journey section
                    MoodJourneySection(
                        moodJourney = uiState.summary?.moodJourney ?: emptyList(),
                        players = uiState.players
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Action buttons
                    ActionButtons(
                        journalEnabled = uiState.journalEnabled,
                        journalSaved = uiState.journalSaved,
                        onSaveToJournal = { viewModel.saveToJournal() },
                        onNewGame = { onStartNewGame(uiState.players.map { it.id }) },
                        onNavigateToPlayers = onNavigateToPlayers
                    )
                }
            }
        }
    }
}

/**
 * Loading view
 */
@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sessie samenvatting genereren...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Players section
 */
@Composable
fun PlayersSection(players: List<PlayerEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Spelers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                players.forEach { player ->
                    PlayerAvatar(
                        player = player,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Player avatar
 */
@Composable
fun PlayerAvatar(
    player: PlayerEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(player.avatarColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Top tags section
 */
@Composable
fun TopTagsSection(tags: List<String>) {
    if (tags.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Populaire onderwerpen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            tags.forEach { tag ->
                Text(
                    text = "• $tag",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Highlights section
 */
@Composable
fun HighlightsSection(
    playerHighlights: Map<String, List<String>>,
    players: List<PlayerEntity>
) {
    if (playerHighlights.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Speler Hoogtepunten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            playerHighlights.forEach { (playerId, highlights) ->
                val player = players.find { it.id == playerId }
                if (player != null && highlights.isNotEmpty()) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    highlights.forEach { highlight ->
                        Text(
                            text = "• $highlight",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Special moments section
 */
@Composable
fun SpecialMomentsSection(
    deepestMoment: String?,
    funniestMoment: String?
) {
    if (deepestMoment == null && funniestMoment == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bijzondere momenten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            deepestMoment?.let {
                Text(
                    text = "Diepste moment:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            funniestMoment?.let {
                Text(
                    text = "Grappigste moment:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Mood journey section
 */
@Composable
fun MoodJourneySection(
    moodJourney: List<Pair<String, Mood>>,
    players: List<PlayerEntity>
) {
    if (moodJourney.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Stemmingsverloop",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            moodJourney.forEach { (playerId, mood) ->
                val player = players.find { it.id == playerId }
                if (player != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(player.avatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = player.name.take(1).uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(getMoodColor(mood))
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = getMoodText(mood),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * Get color for a mood
 */
@Composable
fun getMoodColor(mood: Mood): Color {
    return when (mood) {
        Mood.HAPPY -> Color(0xFFFFEB3B)
        Mood.CALM -> Color(0xFF4CAF50)
        Mood.SERIOUS -> Color(0xFF3F51B5)
        Mood.NERVOUS -> Color(0xFFFF5722)
    }
}

/**
 * Get text description for a mood
 */
fun getMoodText(mood: Mood): String {
    return when (mood) {
        Mood.HAPPY -> "Vrolijk"
        Mood.CALM -> "Ontspannen"
        Mood.SERIOUS -> "Serieus"
        Mood.NERVOUS -> "Nerveus"
    }
}

/**
 * Action buttons
 */
@Composable
fun ActionButtons(
    journalEnabled: Boolean,
    journalSaved: Boolean,
    onSaveToJournal: () -> Unit,
    onNewGame: () -> Unit,
    onNavigateToPlayers: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Save to journal button
        if (journalEnabled) {
            Button(
                onClick = onSaveToJournal,
                enabled = !journalSaved,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (journalSaved) {
                    Icon(Icons.Default.Check, contentDescription = null)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (journalSaved) "Opgeslagen in journal" else stringResource(R.string.reflect_save)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Play again button
        Button(
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nog een keer spelen")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Back to players button
        OutlinedButton(
            onClick = onNavigateToPlayers,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Terug naar spelers")
        }
    }
}