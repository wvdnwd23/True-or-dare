package com.wes.truthdare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Dialog to set session limits for drinking games
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLimitsDialog(
    initialTimeLimit: Int?,
    initialDrinkLimit: Int?,
    onSetLimits: (timeLimit: Int?, drinkLimit: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var timeLimit by remember { mutableStateOf(initialTimeLimit?.toString() ?: "") }
    var drinkLimit by remember { mutableStateOf(initialDrinkLimit?.toString() ?: "") }
    
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Session Limits",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Set limits for your drinking session to promote responsible drinking.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Time limit input
                OutlinedTextField(
                    value = timeLimit,
                    onValueChange = { 
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            timeLimit = it
                        }
                    },
                    label = { Text("Time Limit (minutes)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                
                // Drink limit input
                OutlinedTextField(
                    value = drinkLimit,
                    onValueChange = { 
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            drinkLimit = it
                        }
                    },
                    label = { Text("Drink Limit (count)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onSetLimits(null, null)
                            onDismiss()
                        }
                    ) {
                        Text("Clear Limits")
                    }
                    
                    Row {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                val timeLimitInt = timeLimit.toIntOrNull()
                                val drinkLimitInt = drinkLimit.toIntOrNull()
                                onSetLimits(timeLimitInt, drinkLimitInt)
                                onDismiss()
                            }
                        ) {
                            Text("Set Limits")
                        }
                    }
                }
            }
        }
    }
}