package com.wes.truthdare.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wes.truthdare.R
import com.wes.truthdare.core.agents.Mood
import com.wes.truthdare.ui.theme.CalmColor
import com.wes.truthdare.ui.theme.DareColor
import com.wes.truthdare.ui.theme.HappyColor
import com.wes.truthdare.ui.theme.NervousColor
import com.wes.truthdare.ui.theme.SeriousColor
import com.wes.truthdare.ui.theme.SkipColor
import com.wes.truthdare.ui.theme.StarColor
import com.wes.truthdare.ui.theme.TruthColor
import kotlin.math.sin

/**
 * Game screen for playing Truth or Dare
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    sessionId: String,
    onEndGame: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    var showEndGameDialog by remember { mutableStateOf(false) }
    
    // Load session when the screen is first displayed
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(uiState.currentPlayer?.name ?: "Truth or Dare") 
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { showEndGameDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "End Game")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isAnswering && !uiState.showFollowUp) {
                FloatingActionButton(
                    onClick = { 
                        if (uiState.isStarred) {
                            // Already starred, do nothing
                        } else {
                            viewModel.starQuestion() 
                        }
                    },
                    containerColor = if (uiState.isStarred) StarColor else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (uiState.isStarred) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(R.string.game_star),
                        tint = if (uiState.isStarred) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main game content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Player info and mood
                PlayerInfoSection(
                    playerName = uiState.currentPlayer?.name ?: "",
                    playerColor = uiState.currentPlayer?.avatarColor?.let { Color(it) } ?: Color.Gray,
                    mood = uiState.currentMood,
                    stressLevel = uiState.stressLevel
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Question card
                QuestionCard(
                    question = uiState.currentQuestion?.text ?: "",
                    type = uiState.currentQuestion?.type ?: "truth",
                    isAnswering = uiState.isAnswering
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                if (!uiState.isAnswering && !uiState.showFollowUp) {
                    ActionButtons(
                        onTruth = { viewModel.startAnswering() },
                        onDare = { viewModel.startAnswering() },
                        onSkip = { viewModel.skipQuestion() }
                    )
                }
                
                // Answering UI
                AnimatedVisibility(
                    visible = uiState.isAnswering,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    AnsweringSection(
                        transcript = uiState.transcript,
                        timerSeconds = uiState.timerSeconds,
                        isVoiceModeEnabled = uiState.voiceModeEnabled,
                        onToggleVoiceMode = { viewModel.toggleVoiceMode() },
                        onFinishAnswering = { viewModel.finishAnswering() }
                    )
                }
                
                // Visualizer
                if (uiState.isAnswering && uiState.voiceModeEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AudioVisualizer(
                        mood = uiState.currentMood,
                        isActive = uiState.isAnswering && uiState.voiceModeEnabled
                    )
                }
                
                // Star tags info
                AnimatedVisibility(visible = uiState.isStarred) {
                    StarredTagsInfo(tags = uiState.starredTags)
                }
            }
            
            // Follow-up question overlay
            AnimatedVisibility(
                visible = uiState.showFollowUp,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FollowUpOverlay(
                    question = uiState.followUpQuestion?.text ?: "",
                    onAnswer = { viewModel.answerFollowUp() },
                    onSkip = { viewModel.skipFollowUp() }
                )
            }
            
            // Safety prompt overlay
            AnimatedVisibility(
                visible = uiState.showSafetyPrompt,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SafetyPromptOverlay(
                    onContinue = { /* Handle continue */ },
                    onMilder = { /* Handle milder question */ },
                    onSkip = { viewModel.skipQuestion() }
                )
            }
        }
        
        // End game dialog
        if (showEndGameDialog) {
            EndGameDialog(
                onConfirm = {
                    viewModel.endGame()
                    onEndGame(sessionId)
                    showEndGameDialog = false
                },
                onDismiss = { showEndGameDialog = false }
            )
        }
    }
}

/**
 * Section showing player info and mood
 */
@Composable
fun PlayerInfoSection(
    playerName: String,
    playerColor: Color,
    mood: Mood,
    stressLevel: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(playerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = playerName.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Player name and mood
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mood indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getMoodColor(mood))
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Mood text
                Text(
                    text = getMoodText(mood),
                    style = MaterialTheme.typography.bodySmall
                )
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
        Mood.HAPPY -> HappyColor
        Mood.CALM -> CalmColor
        Mood.SERIOUS -> SeriousColor
        Mood.NERVOUS -> NervousColor
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
 * Card showing the current question
 */
@Composable
fun QuestionCard(
    question: String,
    type: String,
    isAnswering: Boolean
) {
    val backgroundColor = when (type.lowercase()) {
        "truth" -> TruthColor.copy(alpha = 0.1f)
        "dare" -> DareColor.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = when (type.lowercase()) {
        "truth" -> TruthColor
        "dare" -> DareColor
        else -> MaterialTheme.colorScheme.outline
    }
    
    val typeText = when (type.lowercase()) {
        "truth" -> stringResource(R.string.game_truth)
        "dare" -> stringResource(R.string.game_dare)
        else -> type
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Question type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(borderColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Question text
                Text(
                    text = question,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (isAnswering) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Answering indicator
                    Text(
                        text = "Beantwoorden...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Action buttons for truth, dare, and skip
 */
@Composable
fun ActionButtons(
    onTruth: () -> Unit,
    onDare: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Truth button
        Button(
            onClick = onTruth,
            colors = ButtonDefaults.buttonColors(
                containerColor = TruthColor
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.game_truth))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Dare button
        Button(
            onClick = onDare,
            colors = ButtonDefaults.buttonColors(
                containerColor = DareColor
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.game_dare))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Skip button
        OutlinedButton(
            onClick = onSkip,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SkipColor
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.game_skip))
        }
    }
}

/**
 * Section for answering a question
 */
@Composable
fun AnsweringSection(
    transcript: String,
    timerSeconds: Int,
    isVoiceModeEnabled: Boolean,
    onToggleVoiceMode: () -> Unit,
    onFinishAnswering: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer
        Text(
            text = formatTime(timerSeconds),
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Transcript
        if (isVoiceModeEnabled && transcript.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transcript,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Toggle voice mode button
            IconButton(
                onClick = onToggleVoiceMode,
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    if (isVoiceModeEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle Voice Mode",
                    tint = if (isVoiceModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(32.dp))
            
            // Finish answering button
            Button(
                onClick = onFinishAnswering,
                modifier = Modifier.height(56.dp)
            ) {
                Text(stringResource(R.string.game_next))
            }
        }
    }
}

/**
 * Format seconds as MM:SS
 */
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

/**
 * Audio visualizer component
 */
@Composable
fun AudioVisualizer(
    mood: Mood,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")
    val animationValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "audio_wave"
    )
    
    val color = getMoodColor(mood)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        if (isActive) {
            val path = Path()
            path.moveTo(0f, centerY)
            
            for (x in 0..width.toInt() step 5) {
                val xFloat = x.toFloat()
                val amplitude = if (isActive) 30f else 5f
                val frequency = 0.02f
                val phase = animationValue
                
                val y = centerY + amplitude * sin(xFloat * frequency + phase)
                path.lineTo(xFloat, y)
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3f)
            )
        } else {
            // Draw a flat line when not active
            drawLine(
                color = color,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 3f
            )
        }
    }
}

/**
 * Info about starred tags
 */
@Composable
fun StarredTagsInfo(tags: List<String>) {
    if (tags.isEmpty()) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = StarColor
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Gemarkeerd voor vervolgvragen over: ${tags.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Overlay for follow-up questions
 */
@Composable
fun FollowUpOverlay(
    question: String,
    onAnswer: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.game_follow_up),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = question,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.game_skip))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = onAnswer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Beantwoorden")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overlay for safety prompts
 */
@Composable
fun SafetyPromptOverlay(
    onContinue: () -> Unit,
    onMilder: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Veiligheidscheck",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Deze vraag of antwoord bevat mogelijk gevoelige inhoud. Wat wil je doen?",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.safety_continue))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onMilder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.safety_milder))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.safety_skip))
                    }
                }
            }
        }
    }
}

/**
 * Dialog for confirming end game
 */
@Composable
fun EndGameDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Spel beëindigen") },
        text = { Text("Weet je zeker dat je het spel wilt beëindigen?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ja, beëindigen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nee, doorgaan")
            }
        }
    )
}