package com.wes.truthdare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wes.truthdare.ui.screens.drink.Difficulty

/**
 * Badge displaying the difficulty level of a mini-game
 */
@Composable
fun DifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (difficulty) {
        Difficulty.EASY -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),  // Light green background
            Color(0xFF2E7D32),                      // Dark green text
            "Easy"
        )
        Difficulty.MEDIUM -> Triple(
            Color(0xFFFFC107).copy(alpha = 0.2f),  // Light yellow background
            Color(0xFFFF8F00),                      // Dark yellow/orange text
            "Medium"
        )
        Difficulty.HARD -> Triple(
            Color(0xFFF44336).copy(alpha = 0.2f),  // Light red background
            Color(0xFFD32F2F),                      // Dark red text
            "Hard"
        )
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}