package com.wes.truthdare.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wes.truthdare.R

/**
 * Settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayers: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWipeDataDialog by remember { mutableStateOf(false) }
    var showWipeSuccessDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Voice mode setting
            SettingsSwitchItem(
                title = stringResource(R.string.settings_voice_mode),
                description = "Luister naar antwoorden en stel automatisch vervolgvragen",
                isChecked = uiState.voiceModeEnabled,
                onCheckedChange = { viewModel.setVoiceModeEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Journal setting
            SettingsSwitchItem(
                title = stringResource(R.string.settings_journal),
                description = "Sla antwoorden en sessie-samenvattingen op",
                isChecked = uiState.journalEnabled,
                onCheckedChange = { viewModel.setJournalEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Haptics setting
            SettingsSwitchItem(
                title = stringResource(R.string.settings_haptics),
                description = "Trillen bij acties en overgangen",
                isChecked = uiState.hapticsEnabled,
                onCheckedChange = { viewModel.setHapticsEnabled(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Volume setting
            SettingsSliderItem(
                title = stringResource(R.string.settings_volume),
                value = uiState.volumeLevel,
                valueRange = 0f..100f,
                steps = 0,
                valueText = "${uiState.volumeLevel}%",
                onValueChange = { viewModel.setVolumeLevel(it.toInt()) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Deep talk level setting
            SettingsSliderItem(
                title = stringResource(R.string.settings_deep_talk),
                value = uiState.defaultDepthLevel.toFloat(),
                valueRange = 1f..5f,
                steps = 3,
                valueText = "Niveau ${uiState.defaultDepthLevel}",
                onValueChange = { viewModel.setDefaultDepthLevel(it.toInt()) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Wipe data button
            Button(
                onClick = { showWipeDataDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_wipe_data))
            }
        }
        
        // Wipe data confirmation dialog
        if (showWipeDataDialog) {
            AlertDialog(
                onDismissRequest = { showWipeDataDialog = false },
                title = { Text(stringResource(R.string.settings_wipe_data)) },
                text = { Text(stringResource(R.string.settings_wipe_confirm)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.wipeAllData()
                            showWipeDataDialog = false
                            showWipeSuccessDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showWipeDataDialog = false }
                    ) {
                        Text(stringResource(R.string.no))
                    }
                }
            )
        }
        
        // Wipe success dialog
        if (showWipeSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showWipeSuccessDialog = false },
                title = { Text("Data gewist") },
                text = { Text("Alle data is succesvol gewist.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showWipeSuccessDialog = false
                            onNavigateToPlayers()
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

/**
 * Settings item with a switch
 */
@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * Settings item with a slider
 */
@Composable
fun SettingsSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueText: String,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps
            )
        }
    }
}