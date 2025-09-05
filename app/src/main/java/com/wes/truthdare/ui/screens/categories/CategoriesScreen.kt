package com.wes.truthdare.ui.screens.categories

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wes.truthdare.R
import com.wes.truthdare.core.selector.GameMode
import com.wes.truthdare.ui.theme.CalmColor
import com.wes.truthdare.ui.theme.CoolColor
import com.wes.truthdare.ui.theme.HotColor
import com.wes.truthdare.ui.theme.WarmColor
import kotlinx.coroutines.launch

/**
 * Categories screen for selecting game options
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    playerIds: List<String>,
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Set player IDs when the screen is first displayed
    LaunchedEffect(playerIds) {
        viewModel.setPlayerIds(playerIds)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title)) },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Players summary
            PlayersSection(playerNames = uiState.playerNames)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Game mode selection
            GameModeSection(
                selectedMode = uiState.gameMode,
                onModeSelected = { viewModel.setGameMode(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Categories selection
            CategoriesSection(
                selectedCategories = uiState.selectedCategories,
                onCategoryToggled = { viewModel.toggleCategory(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Heat level slider
            HeatLevelSection(
                heatLevel = uiState.heatLevel,
                onHeatLevelChanged = { viewModel.setHeatLevel(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Depth level slider
            DepthLevelSection(
                depthLevel = uiState.depthLevel,
                onDepthLevelChanged = { viewModel.setDepthLevel(it) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Start game button
            Button(
                onClick = {
                    coroutineScope.launch {
                        val sessionId = viewModel.createSession()
                        onNavigateToGame(sessionId)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.categories_continue))
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
                text = "Spelers",
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
 * Section for selecting game mode
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameModeSection(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Spelmodus",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameModeChip(
                mode = GameMode.CASUAL,
                label = "Casual",
                isSelected = selectedMode == GameMode.CASUAL,
                onSelected = { onModeSelected(GameMode.CASUAL) }
            )
            
            GameModeChip(
                mode = GameMode.PARTY,
                label = "Party",
                isSelected = selectedMode == GameMode.PARTY,
                onSelected = { onModeSelected(GameMode.PARTY) }
            )
            
            GameModeChip(
                mode = GameMode.DEEP_TALK,
                label = "Deep Talk",
                isSelected = selectedMode == GameMode.DEEP_TALK,
                onSelected = { onModeSelected(GameMode.DEEP_TALK) }
            )
            
            GameModeChip(
                mode = GameMode.ROMANTIC,
                label = "Romantisch",
                isSelected = selectedMode == GameMode.ROMANTIC,
                onSelected = { onModeSelected(GameMode.ROMANTIC) }
            )
            
            GameModeChip(
                mode = GameMode.FAMILY_FRIENDLY,
                label = "Familie",
                isSelected = selectedMode == GameMode.FAMILY_FRIENDLY,
                onSelected = { onModeSelected(GameMode.FAMILY_FRIENDLY) }
            )
        }
    }
}

/**
 * Chip for selecting game mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameModeChip(
    mode: GameMode,
    label: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
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

/**
 * Section for selecting categories
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesSection(
    selectedCategories: Set<String>,
    onCategoryToggled: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Categorieën",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                category = "casual",
                label = "Alledaags",
                isSelected = selectedCategories.contains("casual"),
                onToggle = { onCategoryToggled("casual") }
            )
            
            CategoryChip(
                category = "party",
                label = "Feest",
                isSelected = selectedCategories.contains("party"),
                onToggle = { onCategoryToggled("party") }
            )
            
            CategoryChip(
                category = "deep",
                label = "Diepgaand",
                isSelected = selectedCategories.contains("deep"),
                onToggle = { onCategoryToggled("deep") }
            )
            
            CategoryChip(
                category = "romantic",
                label = "Romantisch",
                isSelected = selectedCategories.contains("romantic"),
                onToggle = { onCategoryToggled("romantic") }
            )
            
            CategoryChip(
                category = "family",
                label = "Familie",
                isSelected = selectedCategories.contains("family"),
                onToggle = { onCategoryToggled("family") }
            )
            
            CategoryChip(
                category = "friends",
                label = "Vrienden",
                isSelected = selectedCategories.contains("friends"),
                onToggle = { onCategoryToggled("friends") }
            )
            
            CategoryChip(
                category = "funny",
                label = "Grappig",
                isSelected = selectedCategories.contains("funny"),
                onToggle = { onCategoryToggled("funny") }
            )
            
            CategoryChip(
                category = "challenge",
                label = "Uitdagend",
                isSelected = selectedCategories.contains("challenge"),
                onToggle = { onCategoryToggled("challenge") }
            )
            
            CategoryChip(
                category = "personal",
                label = "Persoonlijk",
                isSelected = selectedCategories.contains("personal"),
                onToggle = { onCategoryToggled("personal") }
            )
            
            CategoryChip(
                category = "childhood",
                label = "Jeugd",
                isSelected = selectedCategories.contains("childhood"),
                onToggle = { onCategoryToggled("childhood") }
            )
            
            CategoryChip(
                category = "future",
                label = "Toekomst",
                isSelected = selectedCategories.contains("future"),
                onToggle = { onCategoryToggled("future") }
            )
            
            CategoryChip(
                category = "hypothetical",
                label = "Wat als",
                isSelected = selectedCategories.contains("hypothetical"),
                onToggle = { onCategoryToggled("hypothetical") }
            )
        }
    }
}

/**
 * Chip for selecting a category
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
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

/**
 * Section for adjusting heat level
 */
@Composable
fun HeatLevelSection(
    heatLevel: Int,
    onHeatLevelChanged: (Int) -> Unit
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
                text = "Pikant niveau",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "$heatLevel%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = heatLevel.toFloat(),
            onValueChange = { onHeatLevelChanged(it.toInt()) },
            valueRange = 0f..100f,
            steps = 0,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeatIndicator(level = 0, color = CoolColor, label = "Mild")
            HeatIndicator(level = 33, color = CalmColor, label = "Normaal")
            HeatIndicator(level = 66, color = WarmColor, label = "Pittig")
            HeatIndicator(level = 100, color = HotColor, label = "Heet")
        }
    }
}

/**
 * Heat level indicator
 */
@Composable
fun HeatIndicator(
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
 * Section for adjusting depth level
 */
@Composable
fun DepthLevelSection(
    depthLevel: Int,
    onDepthLevelChanged: (Int) -> Unit
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
                text = "Deep Talk niveau",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Niveau $depthLevel",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = depthLevel.toFloat(),
            onValueChange = { onDepthLevelChanged(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DepthIndicator(level = 1, label = "Oppervlakkig")
            DepthIndicator(level = 3, label = "Persoonlijk")
            DepthIndicator(level = 5, label = "Diepgaand")
        }
    }
}

/**
 * Depth level indicator
 */
@Composable
fun DepthIndicator(
    level: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = level.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}