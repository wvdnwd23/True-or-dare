package com.wes.truthdare.ui.screens.report

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.wes.truthdare.R
import com.wes.truthdare.core.report.HighlightType
import com.wes.truthdare.core.report.ReportStorageOption
import com.wes.truthdare.core.report.Theme
import kotlinx.coroutines.launch
import java.io.File

/**
 * Screen for displaying and exporting reports
 * Can display either the current session report or a saved report
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit,
    reportId: String? = null,
    viewModel: ReportViewModel = hiltViewModel()
) {
    // Load the report if a reportId is provided
    LaunchedEffect(reportId) {
        if (reportId != null) {
            viewModel.loadReport(reportId)
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Handle consent dialog
    if (uiState.showConsentDialog) {
        ConsentDialog(
            onDismiss = { viewModel.dismissConsentDialog() },
            onConsent = { option ->
                viewModel.setReportStorageOption(option)
                viewModel.setConsentGiven(true)
                viewModel.dismissConsentDialog()
            }
        )
    }
    
    // Handle export dialog
    if (uiState.showExportDialog) {
        ExportDialog(
            onDismiss = { viewModel.dismissExportDialog() },
            onExport = { format ->
                scope.launch {
                    val file = when (format) {
                        ExportFormat.PDF -> viewModel.exportToPdf()
                        ExportFormat.CSV -> viewModel.exportToCsv()
                        ExportFormat.JSON -> viewModel.exportToJson()
                    }
                    
                    // Share the file
                    file?.let { shareFile(context, it) }
                }
            }
        )
    }
    
    // Handle settings dialog
    if (uiState.showSettingsDialog) {
        ReportSettingsDialog(
            redactAnswers = uiState.redactAnswers,
            anonymize = uiState.anonymize,
            theme = uiState.theme,
            watermark = uiState.watermark,
            isPremium = uiState.isPremium,
            onDismiss = { viewModel.dismissSettingsDialog() },
            onSave = { redact, anonymize, theme, watermark ->
                viewModel.updateSettings(redact, anonymize, theme, watermark)
                viewModel.dismissSettingsDialog()
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Export button
                    IconButton(onClick = { viewModel.showExportDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.export_report)
                        )
                    }
                    
                    // Settings button
                    IconButton(onClick = { viewModel.showSettingsDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.report_settings)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.report == null) {
            // No report available
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_report_available),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Report content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.report_summary),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Divider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.game_mode))
                                Text(uiState.report.summary.gameMode.toString())
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.players))
                                Text("${uiState.report.summary.playerCount}")
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.rounds))
                                Text("${uiState.report.summary.roundCount}")
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.truths))
                                Text("${uiState.report.summary.truthCount}")
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.dares))
                                Text("${uiState.report.summary.dareCount}")
                            }
                            
                            if (uiState.report.summary.drinkMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.total_shots))
                                    Text("${uiState.report.summary.totalShots}")
                                }
                            }
                        }
                    }
                }
                
                // Player stats section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.player_statistics),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Divider()
                            
                            uiState.report.perPlayer.forEach { player ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = player.playerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.truths_answered))
                                            Text("${player.truthsAnswered}")
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.dares_completed))
                                            Text("${player.daresCompleted}")
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.skipped))
                                            Text("${player.skipped}")
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.completion_rate))
                                            Text("${(player.completionRate * 100).toInt()}%")
                                        }
                                        
                                        if (uiState.report.summary.drinkMode) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(stringResource(R.string.shots_taken))
                                                Text("${player.shotsTaken}")
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                // Highlights section
                if (uiState.report.highlights.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.highlights),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Divider()
                                
                                uiState.report.highlights.forEach { highlight ->
                                    val icon = when (highlight.type) {
                                        HighlightType.MOST_HONEST -> Icons.Default.Verified
                                        HighlightType.DARE_DEVIL -> Icons.Default.Whatshot
                                        HighlightType.MOST_SHOTS -> Icons.Default.LocalBar
                                        HighlightType.MOST_SKIPS -> Icons.Default.SkipNext
                                        HighlightType.FUNNIEST_ANSWER -> Icons.Default.EmojiEmotions
                                        HighlightType.HARDEST_DARE -> Icons.Default.Star
                                        HighlightType.LONGEST_ANSWER -> Icons.Default.Article
                                        HighlightType.QUICKEST_ANSWER -> Icons.Default.Speed
                                    }
                                    
                                    ListItem(
                                        headlineContent = { Text(highlight.title) },
                                        supportingContent = { Text(highlight.description) },
                                        leadingContent = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                    
                                    Divider()
                                }
                            }
                        }
                    }
                }
                
                // Rounds section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.rounds),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Divider()
                            
                            // Show rounds in an expandable list
                            uiState.report.rounds.forEach { round ->
                                var expanded by remember { mutableStateOf(false) }
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
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
                                                text = "Round ${round.roundNumber}: ${round.playerName}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            
                                            IconButton(onClick = { expanded = !expanded }) {
                                                Icon(
                                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
                                                )
                                            }
                                        }
                                        
                                        if (expanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Text(
                                                text = "${round.cardType}: ${round.question}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            if (round.answer != null && !uiState.redactAnswers) {
                                                Text(
                                                    text = stringResource(R.string.answer_label, round.answer),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (round.completed) stringResource(R.string.completed) else stringResource(R.string.skipped),
                                                    color = if (round.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                                
                                                if (uiState.report.summary.drinkMode && round.penalties > 0) {
                                                    Text(
                                                        text = stringResource(R.string.penalties, round.penalties),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for getting consent for report generation
 */
@Composable
fun ConsentDialog(
    onDismiss: () -> Unit,
    onConsent: (ReportStorageOption) -> Unit
) {
    var selectedOption by remember { mutableStateOf(ReportStorageOption.GENERATE_ONCE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.report_consent_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.report_consent_message))
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedOption == ReportStorageOption.SAVE_LOCALLY,
                            onClick = { selectedOption = ReportStorageOption.SAVE_LOCALLY }
                        )
                        Text(
                            text = stringResource(R.string.save_locally),
                            modifier = Modifier.clickable { selectedOption = ReportStorageOption.SAVE_LOCALLY }
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedOption == ReportStorageOption.GENERATE_ONCE,
                            onClick = { selectedOption = ReportStorageOption.GENERATE_ONCE }
                        )
                        Text(
                            text = stringResource(R.string.generate_once),
                            modifier = Modifier.clickable { selectedOption = ReportStorageOption.GENERATE_ONCE }
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedOption == ReportStorageOption.NEVER_SAVE_ANSWERS,
                            onClick = { selectedOption = ReportStorageOption.NEVER_SAVE_ANSWERS }
                        )
                        Text(
                            text = stringResource(R.string.never_save_answers),
                            modifier = Modifier.clickable { selectedOption = ReportStorageOption.NEVER_SAVE_ANSWERS }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConsent(selectedOption) }
            ) {
                Text(stringResource(R.string.continue_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Dialog for exporting a report
 */
@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_report)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.export_format_message))
                
                Button(
                    onClick = { onExport(ExportFormat.PDF) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.export_pdf))
                }
                
                Button(
                    onClick = { onExport(ExportFormat.CSV) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.export_csv))
                }
                
                Button(
                    onClick = { onExport(ExportFormat.JSON) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.export_json))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Dialog for report settings
 */
@Composable
fun ReportSettingsDialog(
    redactAnswers: Boolean,
    anonymize: Boolean,
    theme: Theme,
    watermark: Boolean,
    isPremium: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean, Theme, Boolean) -> Unit
) {
    var redactAnswersState by remember { mutableStateOf(redactAnswers) }
    var anonymizeState by remember { mutableStateOf(anonymize) }
    var themeState by remember { mutableStateOf(theme) }
    var watermarkState by remember { mutableStateOf(watermark) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.report_settings)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Redact answers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = redactAnswersState,
                        onCheckedChange = { redactAnswersState = it }
                    )
                    Text(
                        text = stringResource(R.string.redact_answers),
                        modifier = Modifier.clickable { redactAnswersState = !redactAnswersState }
                    )
                }
                
                // Anonymize names
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = anonymizeState,
                        onCheckedChange = { anonymizeState = it }
                    )
                    Text(
                        text = stringResource(R.string.anonymize_names),
                        modifier = Modifier.clickable { anonymizeState = !anonymizeState }
                    )
                }
                
                // Theme
                Text(
                    text = stringResource(R.string.report_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = themeState == Theme.LIGHT,
                        onClick = { themeState = Theme.LIGHT }
                    )
                    Text(
                        text = stringResource(R.string.light_theme),
                        modifier = Modifier.clickable { themeState = Theme.LIGHT }
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = themeState == Theme.DARK,
                        onClick = { themeState = Theme.DARK }
                    )
                    Text(
                        text = stringResource(R.string.dark_theme),
                        modifier = Modifier.clickable { themeState = Theme.DARK }
                    )
                }
                
                // Watermark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = watermarkState,
                        onCheckedChange = { watermarkState = it },
                        enabled = !isPremium // Only non-premium users can't change this
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.include_watermark),
                            modifier = if (isPremium) Modifier.clickable { watermarkState = !watermarkState } else Modifier
                        )
                        if (!isPremium) {
                            Text(
                                text = stringResource(R.string.premium_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(redactAnswersState, anonymizeState, themeState, watermarkState) }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Export formats
 */
enum class ExportFormat {
    PDF, CSV, JSON
}

/**
 * Share a file using the Android share sheet
 */
private fun shareFile(context: android.content.Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = when {
            file.name.endsWith(".pdf") -> "application/pdf"
            file.name.endsWith(".csv") -> "text/csv"
            file.name.endsWith(".json") -> "application/json"
            else -> "*/*"
        }
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_report)))
}