package com.wes.truthdare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wes.truthdare.R

/**
 * Component for setting session limits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLimitsSelector(
    timeLimit: Int?,
    drinkLimit: Int?,
    onTimeLimitChanged: (Int?) -> Unit,
    onDrinkLimitChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimeLimitDialog by remember { mutableStateOf(false) }
    var showDrinkLimitDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.session_limits),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Set limits for your drinking session to promote responsible drinking.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time Limit
            OutlinedCard(
                onClick = { showTimeLimitDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.time_limit),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = if (timeLimit != null) "$timeLimit ${stringResource(R.string.minutes)}" else "Not set",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Drink Limit
            OutlinedCard(
                onClick = { showDrinkLimitDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.drink_limit),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = if (drinkLimit != null) "$drinkLimit ${stringResource(R.string.drinks)}" else "Not set",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    
    // Time Limit Dialog
    if (showTimeLimitDialog) {
        var timeLimitValue by remember { mutableStateOf(timeLimit?.toString() ?: "") }
        
        AlertDialog(
            onDismissRequest = { showTimeLimitDialog = false },
            title = { Text(stringResource(R.string.set_time_limit)) },
            text = {
                OutlinedTextField(
                    value = timeLimitValue,
                    onValueChange = { timeLimitValue = it.filter { char -> char.isDigit() } },
                    label = { Text(stringResource(R.string.minutes)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = timeLimitValue.toIntOrNull()
                        onTimeLimitChanged(value)
                        showTimeLimitDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimeLimitDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Drink Limit Dialog
    if (showDrinkLimitDialog) {
        var drinkLimitValue by remember { mutableStateOf(drinkLimit?.toString() ?: "") }
        
        AlertDialog(
            onDismissRequest = { showDrinkLimitDialog = false },
            title = { Text(stringResource(R.string.set_drink_limit)) },
            text = {
                OutlinedTextField(
                    value = drinkLimitValue,
                    onValueChange = { drinkLimitValue = it.filter { char -> char.isDigit() } },
                    label = { Text(stringResource(R.string.drinks)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = drinkLimitValue.toIntOrNull()
                        onDrinkLimitChanged(value)
                        showDrinkLimitDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDrinkLimitDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}