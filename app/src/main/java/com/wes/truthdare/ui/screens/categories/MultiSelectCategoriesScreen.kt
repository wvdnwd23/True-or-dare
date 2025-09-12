package com.wes.truthdare.ui.screens.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wes.truthdare.core.data.IntensityLevel
import com.wes.truthdare.core.data.models.Category
import com.wes.truthdare.core.data.models.SubOption
import com.wes.truthdare.ui.theme.CalmColor
import com.wes.truthdare.ui.theme.CoolColor
import com.wes.truthdare.ui.theme.HotColor
import com.wes.truthdare.ui.theme.WarmColor
import kotlinx.coroutines.launch

/**
 * Multi-select categories screen for selecting game options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectCategoriesScreen(
    viewModel: MultiSelectCategoriesViewModel,
    playerIds: List<String>,
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Dialogs state
    var showAgeVerificationDialog by remember { mutableStateOf(false) }
    var showTraumaOptInDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var categoryRequiringVerification by remember { mutableStateOf<Category?>(null) }
    
    // Handle age verification dialog
    if (showAgeVerificationDialog && categoryRequiringVerification != null) {
        AgeVerificationDialog(
            category = categoryRequiringVerification!!,
            onConfirm = {
                viewModel.setAgeVerified(true)
                viewModel.toggleCategory(categoryRequiringVerification!!.id)
                categoryRequiringVerification = null
                showAgeVerificationDialog = false
            },
            onDismiss = {
                categoryRequiringVerification = null
                showAgeVerificationDialog = false
            }
        )
    }
    
    // Handle trauma opt-in dialog
    if (showTraumaOptInDialog) {
        TraumaOptInDialog(
            onConfirm = {
                viewModel.setTraumaHealingOptedIn(true)
                viewModel.toggleCategory("trauma_healing")
                showTraumaOptInDialog = false
            },
            onDismiss = {
                showTraumaOptInDialog = false
            }
        )
    }
    
    // Handle preview dialog
    if (showPreviewDialog) {
        PreviewDialog(
            previewQuestions = uiState.previewQuestions,
            onDismiss = {
                showPreviewDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Clear all selections
                    IconButton(
                        onClick = { viewModel.clearAllSelections() },
                        enabled = uiState.selectedCategoryIds.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear All")
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Players summary
            PlayersSection(playerNames = uiState.playerNames)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Picks
            QuickPicksSection(
                onQuickPickSelected = { preset ->
                    viewModel.applyQuickPick(preset)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selection summary
            SelectionSummary(
                selectionSummary = uiState.getSelectionSummary(),
                onClearAll = {
                    viewModel.clearAllSelections()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categories with sub-options
            CategoriesWithSubOptionsSection(
                categories = uiState.categories,
                selectedCategoryIds = uiState.selectedCategoryIds,
                selectedSubOptionIds = uiState.selectedSubOptionIds,
                onCategoryToggled = { category ->
                    // Check if category requires age verification
                    if (category.isAgeRestricted && !uiState.ageVerified) {
                        categoryRequiringVerification = category
                        showAgeVerificationDialog = true
                    } 
                    // Check if category requires trauma opt-in
                    else if (category.requiresOptIn && category.id == "trauma_healing" && !uiState.traumaHealingOptedIn) {
                        showTraumaOptInDialog = true
                    } 
                    // Otherwise toggle normally
                    else {
                        viewModel.toggleCategory(category.id)
                    }
                },
                onSubOptionToggled = { categoryId, subOptionId ->
                    viewModel.toggleSubOption(categoryId, subOptionId)
                },
                getSubOptionsForCategory = { categoryId ->
                    uiState.getSubOptionsForCategory(categoryId)
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Intensity slider
            IntensitySliderSection(
                intensityValue = uiState.intensitySliderValue,
                onIntensityChanged = { viewModel.setIntensitySliderValue(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preview button
            OutlinedButton(
                onClick = {
                    viewModel.generatePreviewQuestions()
                    showPreviewDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedCategoryIds.isNotEmpty()
            ) {
                Text("Preview Selected Content")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Start game button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val sessionId = viewModel.createSession()
                        onNavigateToGame(sessionId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedCategoryIds.isNotEmpty()
            ) {
                Text("Start Game")
            }
        }
    }
}

/**
 * Section showing selected players
 */
@Composable
fun PlayersSection(playerNames: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Players",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = playerNames.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Quick picks section
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickPicksSection(
    onQuickPickSelected: (QuickPickPreset) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Quick Picks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickPickPreset.getAllPresets().forEach { preset ->
                QuickPickButton(
                    preset = preset,
                    onClick = { onQuickPickSelected(preset) }
                )
            }
        }
    }
}

/**
 * Quick pick button
 */
@Composable
fun QuickPickButton(
    preset: QuickPickPreset,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(preset.name)
    }
}

/**
 * Selection summary
 */
@Composable
fun SelectionSummary(
    selectionSummary: String,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectionSummary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        TextButton(onClick = onClearAll) {
            Text("Clear All")
        }
    }
}

/**
 * Categories with sub-options section
 */
@Composable
fun CategoriesWithSubOptionsSection(
    categories: List<Category>,
    selectedCategoryIds: Set<String>,
    selectedSubOptionIds: Map<String, Set<String>>,
    onCategoryToggled: (Category) -> Unit,
    onSubOptionToggled: (String, String) -> Unit,
    getSubOptionsForCategory: (String) -> List<SubOption>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        categories.forEach { category ->
            CategoryCard(
                category = category,
                isSelected = category.id in selectedCategoryIds,
                selectedSubOptionIds = selectedSubOptionIds[category.id] ?: emptySet(),
                onCategoryToggled = { onCategoryToggled(category) },
                onSubOptionToggled = { subOptionId -> onSubOptionToggled(category.id, subOptionId) },
                subOptions = getSubOptionsForCategory(category.id)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Category card with expandable sub-options
 */
@Composable
fun CategoryCard(
    category: Category,
    isSelected: Boolean,
    selectedSubOptionIds: Set<String>,
    onCategoryToggled: () -> Unit,
    onSubOptionToggled: (String) -> Unit,
    subOptions: List<SubOption>
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category header with toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryToggled() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category emoji
                    Text(
                        text = category.emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Category name and badges
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (category.isAgeRestricted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AgeBadge()
                            }
                            
                            if (category.requiresOptIn) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OptInBadge()
                            }
                        }
                        
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selection indicator
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Expand/collapse icon
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                }
            }
            
            // Sub-options (expandable)
            AnimatedVisibility(
                visible = expanded && isSelected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Divider()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Select sub-options:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SubOptionsGrid(
                        subOptions = subOptions,
                        selectedSubOptionIds = selectedSubOptionIds,
                        onSubOptionToggled = onSubOptionToggled
                    )
                }
            }
        }
    }
}

/**
 * Grid of sub-options
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SubOptionsGrid(
    subOptions: List<SubOption>,
    selectedSubOptionIds: Set<String>,
    onSubOptionToggled: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        subOptions.forEach { subOption ->
            FilterChip(
                selected = subOption.id in selectedSubOptionIds,
                onClick = { onSubOptionToggled(subOption.id) },
                label = { Text(subOption.name) },
                leadingIcon = if (subOption.id in selectedSubOptionIds) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

/**
 * 18+ age restriction badge
 */
@Composable
fun AgeBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = "18+",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * Opt-in required badge
 */
@Composable
fun OptInBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = "Opt-in",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * Intensity slider section
 */
@Composable
fun IntensitySliderSection(
    intensityValue: Int,
    onIntensityChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Content Intensity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val intensityText = when {
                intensityValue < 34 -> "Soft"
                intensityValue < 67 -> "Medium"
                else -> "Hot"
            }
            
            Text(
                text = intensityText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = intensityValue.toFloat(),
            onValueChange = { onIntensityChanged(it.toInt()) },
            valueRange = 0f..100f,
            steps = 0,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IntensityIndicator(level = 0, color = CoolColor, label = "Soft")
            IntensityIndicator(level = 50, color = CalmColor, label = "Medium")
            IntensityIndicator(level = 100, color = HotColor, label = "Hot")
        }
    }
}

/**
 * Intensity level indicator
 */
@Composable
fun IntensityIndicator(
    level: Int,
    color: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Age verification dialog
 */
@Composable
fun AgeVerificationDialog(
    category: Category,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null)
        },
        title = {
            Text("Age Verification Required")
        },
        text = {
            Column {
                Text(
                    "The ${category.name} category contains adult content and requires age verification.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "By continuing, you confirm that all players are 18+ years old.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Confirm 18+")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Trauma & Healing opt-in dialog
 */
@Composable
fun TraumaOptInDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Info, contentDescription = null)
        },
        title = {
            Text("Trauma & Healing Content")
        },
        text = {
            Column {
                Text(
                    "This category contains questions about difficult life experiences, healing, and emotional processing.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "These questions are designed to be supportive, but may bring up sensitive topics. All players should consent to engaging with this content.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Remember that anyone can skip a question at any time without penalty.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Opt In")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Skip Category")
            }
        }
    )
}

/**
 * Preview dialog showing sample questions
 */
@Composable
fun PreviewDialog(
    previewQuestions: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Content Preview")
        },
        text = {
            Column {
                Text(
                    "Here's a sample of questions from your selected categories:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column {
                    previewQuestions.forEach { question ->
                        Text(
                            text = "• $question",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Got It")
            }
        }
    )
}