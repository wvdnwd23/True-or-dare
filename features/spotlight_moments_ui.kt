package com.truthordare.game.ui.spotlight

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.truthordare.game.R
import com.truthordare.game.features.spotlight.*
import com.truthordare.game.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main UI component for displaying Spotlight Moments
 */
@Composable
fun SpotlightMomentCard(
    spotlightMoment: SpotlightMoment,
    onComplete: (SpotlightMomentResult) -> Unit,
    onDismiss: () -> Unit,
    currentPlayerId: String
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Animation states
    var showCard by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showSpotlight by remember { mutableStateOf(false) }
    
    // Sound effect
    val soundEffect = remember(spotlightMoment.soundEffect) {
        when (spotlightMoment.soundEffect) {
            SoundEffectType.FANFARE -> R.raw.fanfare_sound
            SoundEffectType.DRUMROLL -> R.raw.drumroll_sound
            SoundEffectType.CHIME -> R.raw.chime_sound
            SoundEffectType.APPLAUSE -> R.raw.applause_sound
            SoundEffectType.SURPRISE -> R.raw.surprise_sound
        }
    }
    
    // Play sound effect
    LaunchedEffect(spotlightMoment) {
        // Play sound effect here
        
        // Sequence animations
        delay(300)
        showSpotlight = true
        delay(500)
        showCard = true
        delay(700)
        if (spotlightMoment.visualEffect == VisualEffectType.CONFETTI) {
            showConfetti = true
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .zIndex(10f)
    ) {
        // Spotlight effect
        AnimatedVisibility(
            visible = showSpotlight,
            enter = fadeIn(animationSpec = tween(1000)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            SpotlightEffect(spotlightMoment.visualEffect)
        }
        
        // Confetti effect
        if (showConfetti) {
            ConfettiEffect(
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Card content
        AnimatedVisibility(
            visible = showCard,
            enter = scaleIn(animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )) + fadeIn(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            SpotlightCardContent(
                spotlightMoment = spotlightMoment,
                onComplete = { completed ->
                    coroutineScope.launch {
                        // Play completion sound
                        
                        onComplete(
                            SpotlightMomentResult(
                                spotlightId = spotlightMoment.id,
                                playerId = currentPlayerId,
                                completed = completed
                            )
                        )
                    }
                },
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Content of the spotlight moment card
 */
@Composable
fun SpotlightCardContent(
    spotlightMoment: SpotlightMoment,
    onComplete: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(320.dp)
            .wrapContentHeight()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = getSpotlightCardColor(spotlightMoment.type)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spotlight badge
            SpotlightBadge(spotlightMoment.type)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = spotlightMoment.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = spotlightMoment.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Challenge
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CHALLENGE",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = spotlightMoment.challenge,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
            
            // Reward (if any)
            if (!spotlightMoment.reward.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "REWARD",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFFD700)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = spotlightMoment.reward,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onComplete(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Skip")
                }
                
                Button(
                    onClick = { onComplete(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "Accept Challenge",
                        color = getSpotlightCardColor(spotlightMoment.type)
                    )
                }
            }
        }
    }
}

/**
 * Badge showing the spotlight moment type
 */
@Composable
fun SpotlightBadge(type: SpotlightType) {
    val (icon, label) = when (type) {
        SpotlightType.MILESTONE -> Pair(R.drawable.ic_milestone, "MILESTONE")
        SpotlightType.RANDOM -> Pair(R.drawable.ic_random, "SURPRISE")
        SpotlightType.CATEGORY_BASED -> Pair(R.drawable.ic_category, "SPECIAL CATEGORY")
        SpotlightType.COMEBACK -> Pair(R.drawable.ic_comeback, "COMEBACK")
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}

/**
 * Visual spotlight effect
 */
@Composable
fun SpotlightEffect(effectType: VisualEffectType) {
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight")
    
    when (effectType) {
        VisualEffectType.SPOTLIGHT -> {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "spotlight scale"
            )
            
            Box(
                modifier = Modifier
                    .size(600.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )
        }
        
        VisualEffectType.GLOW -> {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow alpha"
            )
            
            Box(
                modifier = Modifier
                    .size(500.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.Yellow.copy(alpha = alpha)
                        )
                    }
            )
        }
        
        VisualEffectType.PULSE -> {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse scale"
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse alpha"
            )
            
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = alpha)
                        )
                    }
            )
        }
        
        VisualEffectType.FIREWORKS -> {
            // Fireworks effect would be more complex
            // This is a simplified version
            FireworksEffect()
        }
        
        else -> {
            // Default effect or no effect
        }
    }
}

/**
 * Confetti animation effect
 */
@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    // This would be a complex particle system animation
    // Simplified version for demonstration
    Box(modifier = modifier) {
        repeat(30) { index ->
            ConfettiParticle(index)
        }
    }
}

/**
 * Individual confetti particle
 */
@Composable
fun ConfettiParticle(seed: Int) {
    val colors = listOf(
        Color(0xFFFF5252), // Red
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFE040FB)  // Purple
    )
    
    val random = java.util.Random(seed.toLong())
    val color = colors[random.nextInt(colors.size)]
    val size = (5 + random.nextInt(10)).dp
    
    val xOffset = random.nextInt(1000) - 500
    val delay = random.nextInt(1000)
    
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val yPosition by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000 + random.nextInt(2000),
                delayMillis = delay,
                easing = LinearEasing
            )
        ),
        label = "confetti y"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f * (random.nextFloat() * 2 - 1),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000 + random.nextInt(1000),
                easing = LinearEasing
            )
        ),
        label = "confetti rotation"
    )
    
    val xPosition by infiniteTransition.animateFloat(
        initialValue = xOffset.toFloat(),
        targetValue = xOffset + (-100 + random.nextInt(200)).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000 + random.nextInt(2000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "confetti x"
    )
    
    Box(
        modifier = Modifier
            .offset(x = xPosition.dp, y = yPosition.dp)
            .size(size)
            .graphicsLayer {
                rotationZ = rotation
            }
            .background(color)
    )
}

/**
 * Fireworks animation effect
 */
@Composable
fun FireworksEffect() {
    // This would be a complex particle system animation
    // Simplified version for demonstration
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(5) { index ->
            FireworkBurst(index)
        }
    }
}

/**
 * Individual firework burst
 */
@Composable
fun FireworkBurst(seed: Int) {
    val colors = listOf(
        Color(0xFFFF5252), // Red
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFE040FB)  // Purple
    )
    
    val random = java.util.Random(seed.toLong())
    val color = colors[random.nextInt(colors.size)]
    
    val xPosition = -200 + random.nextInt(400)
    val yPosition = -200 + random.nextInt(400)
    val delay = random.nextInt(2000)
    
    val infiniteTransition = rememberInfiniteTransition(label = "firework")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "firework scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                delayMillis = delay,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "firework alpha"
    )
    
    Box(
        modifier = Modifier
            .offset(x = xPosition.dp, y = yPosition.dp)
            .size(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
            }
    )
}

/**
 * Get the appropriate background color for the spotlight card based on type
 */
fun getSpotlightCardColor(type: SpotlightType): Color {
    return when (type) {
        SpotlightType.MILESTONE -> Color(0xFF6200EA) // Deep Purple
        SpotlightType.RANDOM -> Color(0xFFD50000) // Red
        SpotlightType.CATEGORY_BASED -> Color(0xFF00BFA5) // Teal
        SpotlightType.COMEBACK -> Color(0xFFFFAB00) // Amber
    }
}

/**
 * Preview for SpotlightMomentCard
 */
@Preview(showBackground = true)
@Composable
fun SpotlightMomentCardPreview() {
    TruthOrDareTheme {
        SpotlightMomentCard(
            spotlightMoment = SpotlightMoment(
                id = "preview_1",
                type = SpotlightType.MILESTONE,
                title = "10th Turn Celebration",
                description = "You've reached the 10th turn! Time for a special challenge.",
                challenge = "The player must answer a truth AND complete a dare",
                reward = "Earn double points for this turn",
                visualEffect = VisualEffectType.CONFETTI,
                soundEffect = SoundEffectType.FANFARE,
                applicableCategories = null
            ),
            onComplete = {},
            onDismiss = {},
            currentPlayerId = "player_1"
        )
    }
}