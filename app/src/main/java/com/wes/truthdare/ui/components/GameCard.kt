package com.wes.truthdare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wes.truthdare.ui.screens.drink.Difficulty

/**
 * A card component for displaying mini-games in the Drink Mode
 */
@Composable
fun GameCard(
    title: String,
    description: String,
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
    onViewInstructions: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (difficulty) {
                Difficulty.EASY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                Difficulty.HARD -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Difficulty: ${difficulty.name.lowercase().capitalize()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            // Add view instructions button if callback is provided
            onViewInstructions?.let {
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onViewInstructions) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("View Instructions")
                }
            }
        }
    }
}

// Extension function to capitalize the first letter of a string
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}