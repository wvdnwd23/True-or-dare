package com.wes.truthdare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wes.truthdare.R
import com.wes.truthdare.ui.screens.drink.Difficulty

/**
 * Dialog for filtering mini-games by difficulty
 */
@Composable
fun DifficultyFilterDialog(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
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
            ) {
                Text(
                    text = "Filter Mini-Games by Difficulty",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // All difficulties option
                DifficultyOption(
                    text = "All Difficulties",
                    isSelected = selectedDifficulty == null,
                    onClick = { onDifficultySelected(null) }
                )
                
                // Easy difficulty option
                DifficultyOption(
                    text = "Easy",
                    isSelected = selectedDifficulty == Difficulty.EASY,
                    onClick = { onDifficultySelected(Difficulty.EASY) },
                    color = Color(0xFF4CAF50) // Green for easy
                )
                
                // Medium difficulty option
                DifficultyOption(
                    text = "Medium",
                    isSelected = selectedDifficulty == Difficulty.MEDIUM,
                    onClick = { onDifficultySelected(Difficulty.MEDIUM) },
                    color = Color(0xFFFFC107) // Yellow for medium
                )
                
                // Hard difficulty option
                DifficultyOption(
                    text = "Hard",
                    isSelected = selectedDifficulty == Difficulty.HARD,
                    onClick = { onDifficultySelected(Difficulty.HARD) },
                    color = Color(0xFFF44336) // Red for hard
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

/**
 * Selectable difficulty option
 */
@Composable
private fun DifficultyOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color = Color.Gray
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}