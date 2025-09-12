package com.wes.truthdare.ui.screens.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wes.truthdare.R
import com.wes.truthdare.core.data.repositories.ReportEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for displaying and managing saved reports
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onNavigateBack: () -> Unit,
    onViewReport: (String) -> Unit,
    viewModel: ReportListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = { Text(stringResource(R.string.delete_report)) },
            text = { Text(stringResource(R.string.delete_report_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmDelete()
                        viewModel.dismissDeleteConfirmation()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDeleteConfirmation() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Handle delete all confirmation dialog
    if (uiState.showDeleteAllConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteAllConfirmation() },
            title = { Text(stringResource(R.string.delete_all_reports)) },
            text = { Text(stringResource(R.string.delete_all_reports_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmDeleteAll()
                        viewModel.dismissDeleteAllConfirmation()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete_all))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDeleteAllConfirmation() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saved_reports)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Delete all button (only show if there are reports)
                    if (uiState.reports.isNotEmpty()) {
                        IconButton(onClick = { viewModel.showDeleteAllConfirmation() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.delete_all_reports)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.reports.isEmpty()) {
            // No reports available
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_saved_reports),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Report list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.reports) { report ->
                    ReportItem(
                        report = report,
                        onReportClick = { onViewReport(report.id) },
                        onDeleteClick = { viewModel.showDeleteConfirmation(report.id) }
                    )
                }
            }
        }
    }
}

/**
 * Item for displaying a report in the list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportItem(
    report: ReportEntity,
    onReportClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onReportClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_report),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.game_mode_label, report.gameMode.toString()),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = formatDate(report.createdAt.toEpochMilli()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.players_count, report.playerCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = stringResource(R.string.rounds_count, report.roundCount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Format a timestamp to a readable date string
 */
private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(date)
}