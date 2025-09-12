package com.wes.truthdare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wes.truthdare.R
import kotlin.math.roundToInt

/**
 * Dialog for setting session limits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLimitsDialog(
    initialTimeLimit: Int?,
    initialDrinkLimit: Int?,
    onSetLimits: (timeLimit: Int?, drinkLimit: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var timeLimit by remember { mutableStateOf(initialTimeLimit ?: 60) }
    var drinkLimit by remember { mutableStateOf(initialDrinkLimit ?: 5) }
    var enableTimeLimit by remember { mutableStateOf(initialTimeLimit != null) }
    var enableDrinkLimit by remember { mutableStateOf(initialDrinkLimit != null) }
    
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
                // Header
                Text(
                    text = stringResource(R.string.session_limits),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time limit section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = stringResource(R.string.time_limit),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Switch(
                        checked = enableTimeLimit,
                        onCheckedChange = { enableTimeLimit = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time limit slider
                if (enableTimeLimit) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp)
                    ) {
                        Text(
                            text = "$timeLimit ${stringResource(R.string.minutes)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Slider(
                            value = timeLimit.toFloat(),
                            onValueChange = { timeLimit = it.roundToInt() },
                            valueRange = 15f..180f,
                            steps = 11, // 15-minute increments
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Drink limit section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = stringResource(R.string.drink_limit),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Switch(
                        checked = enableDrinkLimit,
                        onCheckedChange = { enableDrinkLimit = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Drink limit slider
                if (enableDrinkLimit) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp)
                    ) {
                        Text(
                            text = "$drinkLimit ${stringResource(R.string.drinks)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Slider(
                            value = drinkLimit.toFloat(),
                            onValueChange = { drinkLimit = it.roundToInt() },
                            valueRange = 1f..20f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = {
                            onSetLimits(
                                if (enableTimeLimit) timeLimit else null,
                                if (enableDrinkLimit) drinkLimit else null
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}