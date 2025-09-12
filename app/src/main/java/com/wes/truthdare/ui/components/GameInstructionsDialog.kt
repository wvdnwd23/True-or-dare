package com.wes.truthdare.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wes.truthdare.ui.screens.drink.MiniGame

/**
 * Dialog to display mini-game instructions with illustrations
 */
@Composable
fun GameInstructionsDialog(
    miniGame: MiniGame,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game title
                Text(
                    text = miniGame.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Difficulty badge
                DifficultyBadge(
                    difficulty = miniGame.difficulty,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Game illustration
                // This would use a resource based on the game name
                // For now, we'll use a placeholder
                val illustrationResId = getGameIllustrationResource(miniGame.title)
                if (illustrationResId != null) {
                    Image(
                        painter = painterResource(id = illustrationResId),
                        contentDescription = "${miniGame.title} illustration",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Game description
                Text(
                    text = miniGame.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // Additional instructions (if available)
                val additionalInstructions = getAdditionalInstructions(miniGame.title)
                if (additionalInstructions.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Additional Instructions:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    
                    additionalInstructions.forEach { instruction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                            )
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}

/**
 * Get the illustration resource ID for a given game
 * In a real implementation, this would map game titles to actual drawable resources
 */
private fun getGameIllustrationResource(gameTitle: String): Int? {
    // This would return actual drawable resource IDs
    // For now, we'll return null as we don't have the actual resources
    return null
}

/**
 * Get additional instructions for a given game
 */
private fun getAdditionalInstructions(gameTitle: String): List<String> {
    return when (gameTitle) {
        "Never Have I Ever" -> listOf(
            "Players take turns making statements starting with 'Never have I ever...'",
            "Anyone who has done the stated action must drink",
            "Try to think of things you haven't done but others might have",
            "Keep track of who drinks the most for fun statistics"
        )
        "Beer Pong" -> listOf(
            "Set up 10 cups in a triangle formation at each end of a table",
            "Fill each cup with a small amount of beer or other drink",
            "Take turns throwing ping pong balls into the opponent's cups",
            "When a ball lands in a cup, the defending player must drink that cup",
            "First team to eliminate all of the opponent's cups wins"
        )
        "Flip Cup" -> listOf(
            "Divide into two teams of equal size",
            "Line up on opposite sides of a table",
            "Each player has a cup with a small amount of drink",
            "First player on each team drinks, then places the cup on the edge of the table",
            "Player must flip the cup so it lands upside down before the next player can start",
            "First team to have all players complete the task wins"
        )
        "Kings Cup" -> listOf(
            "Place a cup in the center surrounded by a circle of cards",
            "Each card rank has a specific rule:",
            "Ace: Waterfall (everyone drinks, can't stop until person to right stops)",
            "2: You (choose someone to drink)",
            "3: Me (you drink)",
            "4: Floor (last to touch floor drinks)",
            "5: Guys (all men drink)",
            "6: Chicks (all women drink)",
            "7: Heaven (last to point up drinks)",
            "8: Mate (choose drinking buddy who drinks when you do)",
            "9: Rhyme (rhyming chain, fail = drink)",
            "10: Categories (category chain, fail = drink)",
            "Jack: Rule (create a rule, breaking it = drink)",
            "Queen: Question (answer a question = drink)",
            "King: Pour (pour some drink into center cup)",
            "Person who draws 4th King drinks center cup"
        )
        // Add more games as needed
        else -> emptyList()
    }
}