package com.truthordare.game.features.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truthordare.game.models.*
import com.truthordare.game.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Data collector for end-of-game report statistics
 */
class GameStatisticsCollector(
    private val gameSessionRepository: GameSessionRepository
) {
    /**
     * Collect all statistics for the end-of-game report
     */
    fun collectGameStatistics(gameSessionId: String): GameSessionStatistics {
        val gameSession = gameSessionRepository.getGameSession(gameSessionId)
        
        return GameSessionStatistics(
            gameId = gameSession.id,
            duration = gameSession.endTime - gameSession.startTime,
            totalTurns = gameSession.turns.size,
            categoryStatistics = calculateCategoryStatistics(gameSession),
            playerStatistics = calculatePlayerStatistics(gameSession),
            spotlightMoments = gameSession.spotlightMoments.size,
            achievements = calculateAchievements(gameSession)
        )
    }
    
    /**
     * Calculate statistics for each category used in the game
     */
    private fun calculateCategoryStatistics(gameSession: GameSession): Map<String, CategoryStatistics> {
        val categoryStats = mutableMapOf<String, CategoryStatistics>()
        
        // Group challenges by category
        val challengesByCategory = gameSession.challenges.groupBy { it.category }
        
        // Calculate statistics for each category
        challengesByCategory.forEach { (categoryId, challenges) ->
            val category = gameSession.categories.find { it.id == categoryId }
            
            val completedChallenges = challenges.count { it.completed }
            val skippedChallenges = challenges.count { !it.completed }
            val pointsEarned = challenges.sumOf { it.pointsEarned }
            val timeSpent = challenges.sumOf { it.duration }
            
            // Calculate player performance for this category
            val playerPerformance = calculatePlayerCategoryPerformance(challenges, gameSession.players)
            
            // Count spotlight moments for this category
            val spotlightMoments = gameSession.spotlightMoments.count { 
                it.applicableCategories?.contains(categoryId) == true 
            }
            
            // Calculate streaks
            val streaks = calculateCategoryStreaks(challenges)
            
            // Determine achievements
            val achievements = determineAchievements(categoryId, challenges, gameSession)
            
            categoryStats[categoryId] = CategoryStatistics(
                categoryId = categoryId,
                categoryName = category?.name ?: "Unknown",
                challengesCount = challenges.size,
                completedChallenges = completedChallenges,
                skippedChallenges = skippedChallenges,
                pointsEarned = pointsEarned,
                timeSpent = timeSpent,
                playerPerformance = playerPerformance,
                spotlightMoments = spotlightMoments,
                streaks = streaks,
                achievements = achievements
            )
        }
        
        return categoryStats
    }
    
    /**
     * Calculate player performance for a specific category
     */
    private fun calculatePlayerCategoryPerformance(
        challenges: List<Challenge>,
        players: List<Player>
    ): Map<String, PlayerCategoryPerformance> {
        val playerPerformance = mutableMapOf<String, PlayerCategoryPerformance>()
        
        players.forEach { player ->
            val playerChallenges = challenges.filter { it.playerId == player.id }
            
            if (playerChallenges.isNotEmpty()) {
                val challengesReceived = playerChallenges.size
                val challengesCompleted = playerChallenges.count { it.completed }
                val pointsEarned = playerChallenges.sumOf { it.pointsEarned }
                val averageResponseTime = playerChallenges
                    .filter { it.responseTime > 0 }
                    .map { it.responseTime }
                    .average()
                    .takeIf { !it.isNaN() }
                    ?.toLong() ?: 0
                
                playerPerformance[player.id] = PlayerCategoryPerformance(
                    playerId = player.id,
                    playerName = player.name,
                    challengesReceived = challengesReceived,
                    challengesCompleted = challengesCompleted,
                    pointsEarned = pointsEarned,
                    averageResponseTime = averageResponseTime
                )
            }
        }
        
        return playerPerformance
    }
    
    /**
     * Calculate streaks of completed challenges for a category
     */
    private fun calculateCategoryStreaks(challenges: List<Challenge>): Int {
        val sortedChallenges = challenges.sortedBy { it.timestamp }
        var currentStreak = 0
        var maxStreak = 0
        
        sortedChallenges.forEach { challenge ->
            if (challenge.completed) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 0
            }
        }
        
        return maxStreak
    }
    
    /**
     * Calculate player statistics across all categories
     */
    private fun calculatePlayerStatistics(gameSession: GameSession): Map<String, PlayerStatistics> {
        val playerStats = mutableMapOf<String, PlayerStatistics>()
        
        gameSession.players.forEach { player ->
            val playerChallenges = gameSession.challenges.filter { it.playerId == player.id }
            
            val totalChallenges = playerChallenges.size
            val completedChallenges = playerChallenges.count { it.completed }
            val skippedChallenges = playerChallenges.count { !it.completed }
            val totalPoints = playerChallenges.sumOf { it.pointsEarned }
            
            // Calculate category breakdown
            val categoryBreakdown = playerChallenges
                .groupBy { it.category }
                .mapValues { (_, challenges) -> challenges.size }
            
            // Calculate favorite category
            val favoriteCategory = categoryBreakdown.entries
                .maxByOrNull { it.value }
                ?.key
            
            // Calculate best category (highest completion rate)
            val bestCategory = playerChallenges
                .groupBy { it.category }
                .mapValues { (_, challenges) -> 
                    challenges.count { it.completed }.toFloat() / challenges.size 
                }
                .entries
                .filter { it.value > 0 && playerChallenges.count { c -> c.category == it.key } >= 3 }
                .maxByOrNull { it.value }
                ?.key
            
            playerStats[player.id] = PlayerStatistics(
                playerId = player.id,
                playerName = player.name,
                totalChallenges = totalChallenges,
                completedChallenges = completedChallenges,
                skippedChallenges = skippedChallenges,
                totalPoints = totalPoints,
                categoryBreakdown = categoryBreakdown,
                favoriteCategory = favoriteCategory,
                bestCategory = bestCategory,
                achievements = calculatePlayerAchievements(player.id, gameSession)
            )
        }
        
        return playerStats
    }
    
    /**
     * Determine achievements for a specific category
     */
    private fun determineAchievements(
        categoryId: String,
        challenges: List<Challenge>,
        gameSession: GameSession
    ): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        
        // Category mastery achievement
        val completionRate = challenges.count { it.completed }.toFloat() / challenges.size
        if (completionRate >= 0.8f && challenges.size >= 5) {
            achievements.add(
                Achievement(
                    id = "category_master_$categoryId",
                    name = "Category Master",
                    description = "Completed 80% or more challenges in this category",
                    iconResId = "ic_achievement_master"
                )
            )
        }
        
        // Category explorer achievement
        if (challenges.size >= 10) {
            achievements.add(
                Achievement(
                    id = "category_explorer_$categoryId",
                    name = "Category Explorer",
                    description = "Experienced 10 or more challenges in this category",
                    iconResId = "ic_achievement_explorer"
                )
            )
        }
        
        // Streak achievement
        val streak = calculateCategoryStreaks(challenges)
        if (streak >= 3) {
            achievements.add(
                Achievement(
                    id = "category_streak_$categoryId",
                    name = "Category Streak",
                    description = "Completed $streak challenges in a row in this category",
                    iconResId = "ic_achievement_streak"
                )
            )
        }
        
        return achievements
    }
    
    /**
     * Calculate player-specific achievements
     */
    private fun calculatePlayerAchievements(playerId: String, gameSession: GameSession): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        val playerChallenges = gameSession.challenges.filter { it.playerId == playerId }
        
        // Completion rate achievement
        val completionRate = playerChallenges.count { it.completed }.toFloat() / playerChallenges.size
        if (completionRate >= 0.9f && playerChallenges.size >= 10) {
            achievements.add(
                Achievement(
                    id = "completion_master_$playerId",
                    name = "Completion Master",
                    description = "Completed 90% or more of all challenges",
                    iconResId = "ic_achievement_completion"
                )
            )
        }
        
        // Category diversity achievement
        val uniqueCategories = playerChallenges.map { it.category }.distinct()
        if (uniqueCategories.size >= 5) {
            achievements.add(
                Achievement(
                    id = "category_diversity_$playerId",
                    name = "Category Diversity",
                    description = "Experienced challenges from 5 or more different categories",
                    iconResId = "ic_achievement_diversity"
                )
            )
        }
        
        // Quick responder achievement
        val averageResponseTime = playerChallenges
            .filter { it.responseTime > 0 }
            .map { it.responseTime }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0
        
        if (averageResponseTime > 0 && averageResponseTime < 5000 && playerChallenges.size >= 5) {
            achievements.add(
                Achievement(
                    id = "quick_responder_$playerId",
                    name = "Quick Responder",
                    description = "Average response time under 5 seconds",
                    iconResId = "ic_achievement_quick"
                )
            )
        }
        
        return achievements
    }
    
    /**
     * Calculate overall game achievements
     */
    private fun calculateAchievements(gameSession: GameSession): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        
        // Long game achievement
        if (gameSession.turns.size >= 30) {
            achievements.add(
                Achievement(
                    id = "long_game",
                    name = "Marathon Session",
                    description = "Completed 30 or more turns in a single game",
                    iconResId = "ic_achievement_marathon"
                )
            )
        }
        
        // Category diversity achievement
        val uniqueCategories = gameSession.challenges.map { it.category }.distinct()
        if (uniqueCategories.size >= 8) {
            achievements.add(
                Achievement(
                    id = "game_diversity",
                    name = "Category Explorer",
                    description = "Used 8 or more different categories in a single game",
                    iconResId = "ic_achievement_explorer"
                )
            )
        }
        
        // Spotlight achievement
        if (gameSession.spotlightMoments.size >= 5) {
            achievements.add(
                Achievement(
                    id = "spotlight_master",
                    name = "Spotlight Master",
                    description = "Experienced 5 or more spotlight moments in a single game",
                    iconResId = "ic_achievement_spotlight"
                )
            )
        }
        
        return achievements
    }
}

/**
 * Main UI component for the End-of-Game Report
 */
@Composable
fun EndOfGameReport(
    gameStatistics: GameSessionStatistics,
    onPlayAgain: () -> Unit,
    onShare: () -> Unit,
    onExit: () -> Unit
) {
    var currentSection by remember { mutableStateOf(ReportSection.SUMMARY) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Report header
        ReportHeader(gameStatistics)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section tabs
        ReportSectionTabs(
            currentSection = currentSection,
            onSectionSelected = { currentSection = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section content
        when (currentSection) {
            ReportSection.SUMMARY -> SummarySection(gameStatistics)
            ReportSection.CATEGORIES -> CategoriesSection(gameStatistics)
            ReportSection.PLAYERS -> PlayersSection(gameStatistics)
            ReportSection.ACHIEVEMENTS -> AchievementsSection(gameStatistics)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        ReportActions(
            onPlayAgain = onPlayAgain,
            onShare = onShare,
            onExit = onExit
        )
    }
}

/**
 * Report header with game summary
 */
@Composable
fun ReportHeader(gameStatistics: GameSessionStatistics) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Report",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Game Duration: ${formatDuration(gameStatistics.duration)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Total Turns: ${gameStatistics.totalTurns}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Categories Used: ${gameStatistics.categoryStatistics.size}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Tabs for navigating between report sections
 */
@Composable
fun ReportSectionTabs(
    currentSection: ReportSection,
    onSectionSelected: (ReportSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        ReportSection.values().forEach { section ->
            val isSelected = section == currentSection
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onSectionSelected(section) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Summary section of the report
 */
@Composable
fun SummarySection(gameStatistics: GameSessionStatistics) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GameSummaryCard(gameStatistics)
        }
        
        item {
            TopCategoriesCard(gameStatistics)
        }
        
        item {
            TopPlayersCard(gameStatistics)
        }
        
        item {
            SpotlightMomentsCard(gameStatistics)
        }
    }
}

/**
 * Game summary card
 */
@Composable
fun GameSummaryCard(gameStatistics: GameSessionStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Game Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calculate overall statistics
            val totalChallenges = gameStatistics.categoryStatistics.values
                .sumOf { it.challengesCount }
            
            val completedChallenges = gameStatistics.categoryStatistics.values
                .sumOf { it.completedChallenges }
            
            val completionRate = if (totalChallenges > 0) {
                completedChallenges * 100 / totalChallenges
            } else {
                0
            }
            
            val totalPoints = gameStatistics.categoryStatistics.values
                .sumOf { it.pointsEarned }
            
            // Display statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "Challenges",
                    value = totalChallenges.toString(),
                    icon = Icons.Default.EmojiEvents
                )
                
                StatisticItem(
                    label = "Completion",
                    value = "$completionRate%",
                    icon = Icons.Default.CheckCircle
                )
                
                StatisticItem(
                    label = "Points",
                    value = totalPoints.toString(),
                    icon = Icons.Default.Star
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Completion rate progress bar
            Text(
                text = "Challenge Completion Rate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { completionRate / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$completedChallenges of $totalChallenges challenges completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Top categories card
 */
@Composable
fun TopCategoriesCard(gameStatistics: GameSessionStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Top Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sort categories by usage
            val sortedCategories = gameStatistics.categoryStatistics.values
                .sortedByDescending { it.challengesCount }
                .take(3)
            
            // Display top categories
            sortedCategories.forEachIndexed { index, category ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                TopCategoryItem(
                    rank = index + 1,
                    category = category,
                    totalChallenges = gameStatistics.categoryStatistics.values
                        .sumOf { it.challengesCount }
                )
            }
        }
    }
}

/**
 * Top category item
 */
@Composable
fun TopCategoryItem(
    rank: Int,
    category: CategoryStatistics,
    totalChallenges: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    when (rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Category info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${category.challengesCount} challenges (${calculatePercentage(category.challengesCount, totalChallenges)}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Completion rate
        val completionRate = if (category.challengesCount > 0) {
            category.completedChallenges * 100 / category.challengesCount
        } else {
            0
        }
        
        CompletionRateIndicator(
            completionRate = completionRate,
            size = 40.dp
        )
    }
}

/**
 * Circular completion rate indicator
 */
@Composable
fun CompletionRateIndicator(
    completionRate: Int,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background circle
            drawArc(
                color = MaterialTheme.colorScheme.surfaceVariant,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Progress arc
            drawArc(
                color = when {
                    completionRate >= 80 -> Color(0xFF4CAF50) // Green
                    completionRate >= 50 -> Color(0xFFFFC107) // Amber
                    else -> Color(0xFFF44336) // Red
                },
                startAngle = -90f,
                sweepAngle = 3.6f * completionRate,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Text(
            text = "$completionRate%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Top players card
 */
@Composable
fun TopPlayersCard(gameStatistics: GameSessionStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Player Rankings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sort players by points
            val sortedPlayers = gameStatistics.playerStatistics.values
                .sortedByDescending { it.totalPoints }
            
            // Display player rankings
            sortedPlayers.forEachIndexed { index, player ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                PlayerRankingItem(
                    rank = index + 1,
                    player = player
                )
            }
        }
    }
}

/**
 * Player ranking item
 */
@Composable
fun PlayerRankingItem(
    rank: Int,
    player: PlayerStatistics
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    when (rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Player info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = player.playerName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Show best category if available
            player.bestCategory?.let { bestCategory ->
                Text(
                    text = "Best in: $bestCategory",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Points
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${player.totalPoints}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "points",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Spotlight moments card
 */
@Composable
fun SpotlightMomentsCard(gameStatistics: GameSessionStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Spotlight Moments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (gameStatistics.spotlightMoments > 0) {
                Text(
                    text = "Your game featured ${gameStatistics.spotlightMoments} special spotlight moments!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "These moments added excitement and unique challenges to your gameplay experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "No spotlight moments occurred in this game.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Play longer games to increase your chances of experiencing special spotlight moments!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Categories section of the report
 */
@Composable
fun CategoriesSection(gameStatistics: GameSessionStatistics) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Category distribution chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Category Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pie chart for category distribution
                CategoryPieChart(
                    categories = gameStatistics.categoryStatistics.values.toList(),
                    onCategorySelected = { categoryId ->
                        selectedCategory = if (selectedCategory == categoryId) null else categoryId
                    },
                    selectedCategory = selectedCategory
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category details
        if (selectedCategory != null) {
            gameStatistics.categoryStatistics[selectedCategory]?.let { category ->
                CategoryDetailsCard(category)
            }
        } else {
            // Category list if none selected
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gameStatistics.categoryStatistics.values.toList()) { category ->
                    CategoryListItem(
                        category = category,
                        onClick = { selectedCategory = category.categoryId }
                    )
                }
            }
        }
    }
}

/**
 * Pie chart for category distribution
 */
@Composable
fun CategoryPieChart(
    categories: List<CategoryStatistics>,
    onCategorySelected: (String) -> Unit,
    selectedCategory: String?
) {
    val totalChallenges = categories.sumOf { it.challengesCount }
    
    // Category colors
    val categoryColors = remember {
        listOf(
            Color(0xFF2196F3), // Blue
            Color(0xFF4CAF50), // Green
            Color(0xFFFFC107), // Amber
            Color(0xFFF44336), // Red
            Color(0xFF9C27B0), // Purple
            Color(0xFF00BCD4), // Cyan
            Color(0xFFFF9800), // Orange
            Color(0xFF795548), // Brown
            Color(0xFF607D8B), // Blue Grey
            Color(0xFF009688), // Teal
            Color(0xFFE91E63), // Pink
            Color(0xFF673AB7), // Deep Purple
            Color(0xFF3F51B5), // Indigo
            Color(0xFFCDDC39)  // Lime
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .clickable { }
        ) {
            var startAngle = 0f
            
            categories.forEachIndexed { index, category ->
                val sweepAngle = 360f * category.challengesCount / totalChallenges
                val color = categoryColors[index % categoryColors.size]
                
                // Draw arc with slight padding for selected segment
                val isSelected = category.categoryId == selectedCategory
                val padding = if (isSelected) 8.dp.toPx() else 0f
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(padding / 2, padding / 2),
                    size = Size(size.width - padding, size.height - padding)
                )
                
                // Store the center angle for click detection
                val centerAngle = startAngle + sweepAngle / 2
                
                // Click detection would be implemented here in a real app
                
                startAngle += sweepAngle
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Legend
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val color = categoryColors[index % categoryColors.size]
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelected(category.categoryId) }
                    .background(
                        if (category.categoryId == selectedCategory)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        else
                            Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color, shape = RoundedCornerShape(2.dp))
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Category name
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // Challenge count and percentage
                Text(
                    text = "${category.challengesCount} (${calculatePercentage(category.challengesCount, totalChallenges)}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Category list item
 */
@Composable
fun CategoryListItem(
    category: CategoryStatistics,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${category.challengesCount} challenges, ${category.completedChallenges} completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Completion rate
            val completionRate = if (category.challengesCount > 0) {
                category.completedChallenges * 100 / category.challengesCount
            } else {
                0
            }
            
            CompletionRateIndicator(
                completionRate = completionRate,
                size = 40.dp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Category details card
 */
@Composable
fun CategoryDetailsCard(category: CategoryStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "Challenges",
                    value = category.challengesCount.toString(),
                    icon = Icons.Default.EmojiEvents
                )
                
                val completionRate = if (category.challengesCount > 0) {
                    category.completedChallenges * 100 / category.challengesCount
                } else {
                    0
                }
                
                StatisticItem(
                    label = "Completion",
                    value = "$completionRate%",
                    icon = Icons.Default.CheckCircle
                )
                
                StatisticItem(
                    label = "Points",
                    value = category.pointsEarned.toString(),
                    icon = Icons.Default.Star
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Player performance
            Text(
                text = "Player Performance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            category.playerPerformance.values.forEach { playerPerformance ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = playerPerformance.playerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    val playerCompletionRate = if (playerPerformance.challengesReceived > 0) {
                        playerPerformance.challengesCompleted * 100 / playerPerformance.challengesReceived
                    } else {
                        0
                    }
                    
                    Text(
                        text = "$playerCompletionRate% completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Achievements
            if (category.achievements.isNotEmpty()) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                category.achievements.forEach { achievement ->
                    AchievementItem(achievement)
                }
            }
        }
    }
}

/**
 * Players section of the report
 */
@Composable
fun PlayersSection(gameStatistics: GameSessionStatistics) {
    var selectedPlayer by remember { mutableStateOf<String?>(null) }
    
    if (selectedPlayer != null) {
        // Player details view
        gameStatistics.playerStatistics[selectedPlayer]?.let { player ->
            PlayerDetailsCard(
                player = player,
                onBack = { selectedPlayer = null }
            )
        }
    } else {
        // Player list view
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gameStatistics.playerStatistics.values.toList()) { player ->
                PlayerListItem(
                    player = player,
                    onClick = { selectedPlayer = player.playerId }
                )
            }
        }
    }
}

/**
 * Player list item
 */
@Composable
fun PlayerListItem(
    player: PlayerStatistics,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = player.playerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val completionRate = if (player.totalChallenges > 0) {
                    player.completedChallenges * 100 / player.totalChallenges
                } else {
                    0
                }
                
                Text(
                    text = "${player.totalChallenges} challenges, $completionRate% completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Points
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${player.totalPoints}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Player details card
 */
@Composable
fun PlayerDetailsCard(
    player: PlayerStatistics,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = player.playerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Player statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "Challenges",
                    value = player.totalChallenges.toString(),
                    icon = Icons.Default.EmojiEvents
                )
                
                val completionRate = if (player.totalChallenges > 0) {
                    player.completedChallenges * 100 / player.totalChallenges
                } else {
                    0
                }
                
                StatisticItem(
                    label = "Completion",
                    value = "$completionRate%",
                    icon = Icons.Default.CheckCircle
                )
                
                StatisticItem(
                    label = "Points",
                    value = player.totalPoints.toString(),
                    icon = Icons.Default.Star
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category breakdown
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category distribution chart for this player
            PlayerCategoryChart(player)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Favorite and best categories
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                player.favoriteCategory?.let { favoriteCategory ->
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Most Played",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = favoriteCategory,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                player.bestCategory?.let { bestCategory ->
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Best Performance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = bestCategory,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Achievements
            if (player.achievements.isNotEmpty()) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                player.achievements.forEach { achievement ->
                    AchievementItem(achievement)
                }
            }
        }
    }
}

/**
 * Player category distribution chart
 */
@Composable
fun PlayerCategoryChart(player: PlayerStatistics) {
    // This would be a bar chart showing category distribution
    // Simplified implementation for demonstration
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        player.categoryBreakdown.entries.sortedByDescending { it.value }.forEach { (category, count) ->
            val percentage = count * 100 / player.totalChallenges
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(100.dp)
                )
                
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * Achievements section of the report
 */
@Composable
fun AchievementsSection(gameStatistics: GameSessionStatistics) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game achievements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Game Achievements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (gameStatistics.achievements.isNotEmpty()) {
                        gameStatistics.achievements.forEach { achievement ->
                            AchievementItem(achievement)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            text = "No game achievements unlocked in this session.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Player achievements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Player Achievements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val playerAchievements = gameStatistics.playerStatistics.values
                        .flatMap { player -> 
                            player.achievements.map { achievement ->
                                Pair(player.playerName, achievement)
                            }
                        }
                    
                    if (playerAchievements.isNotEmpty()) {
                        playerAchievements.forEach { (playerName, achievement) ->
                            PlayerAchievementItem(playerName, achievement)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            text = "No player achievements unlocked in this session.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Category achievements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Category Achievements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val categoryAchievements = gameStatistics.categoryStatistics.values
                        .flatMap { category -> 
                            category.achievements.map { achievement ->
                                Pair(category.categoryName, achievement)
                            }
                        }
                    
                    if (categoryAchievements.isNotEmpty()) {
                        categoryAchievements.forEach { (categoryName, achievement) ->
                            CategoryAchievementItem(categoryName, achievement)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            text = "No category achievements unlocked in this session.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Achievement item
 */
@Composable
fun AchievementItem(achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Achievement icon
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFD700), // Gold
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Achievement info
        Column {
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Player achievement item
 */
@Composable
fun PlayerAchievementItem(playerName: String, achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Achievement icon
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFD700), // Gold
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Achievement info
        Column {
            Text(
                text = "$playerName - ${achievement.name}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Category achievement item
 */
@Composable
fun CategoryAchievementItem(categoryName: String, achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Achievement icon
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFD700), // Gold
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Achievement info
        Column {
            Text(
                text = "$categoryName - ${achievement.name}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Statistic item
 */
@Composable
fun StatisticItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Report action buttons
 */
@Composable
fun ReportActions(
    onPlayAgain: () -> Unit,
    onShare: () -> Unit,
    onExit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onPlayAgain,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Play Again")
        }
        
        Button(
            onClick = onShare,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share")
        }
        
        Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Exit")
        }
    }
}

/**
 * Report sections
 */
enum class ReportSection(val title: String) {
    SUMMARY("Summary"),
    CATEGORIES("Categories"),
    PLAYERS("Players"),
    ACHIEVEMENTS("Achievements")
}

/**
 * Helper function to calculate percentage
 */
fun calculatePercentage(value: Int, total: Int): Int {
    return if (total > 0) {
        (value * 100 / total)
    } else {
        0
    }
}

/**
 * Helper function to format duration
 */
fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
}

/**
 * Data classes for statistics
 */
data class GameSessionStatistics(
    val gameId: String,
    val duration: Long,
    val totalTurns: Int,
    val categoryStatistics: Map<String, CategoryStatistics>,
    val playerStatistics: Map<String, PlayerStatistics>,
    val spotlightMoments: Int,
    val achievements: List<Achievement>
)

data class CategoryStatistics(
    val categoryId: String,
    val categoryName: String,
    val challengesCount: Int,
    val completedChallenges: Int,
    val skippedChallenges: Int,
    val pointsEarned: Int,
    val timeSpent: Long, // in milliseconds
    val playerPerformance: Map<String, PlayerCategoryPerformance>,
    val spotlightMoments: Int,
    val streaks: Int,
    val achievements: List<Achievement>
)

data class PlayerCategoryPerformance(
    val playerId: String,
    val playerName: String,
    val challengesReceived: Int,
    val challengesCompleted: Int,
    val pointsEarned: Int,
    val averageResponseTime: Long
)

data class PlayerStatistics(
    val playerId: String,
    val playerName: String,
    val totalChallenges: Int,
    val completedChallenges: Int,
    val skippedChallenges: Int,
    val totalPoints: Int,
    val categoryBreakdown: Map<String, Int>,
    val favoriteCategory: String?,
    val bestCategory: String?,
    val achievements: List<Achievement>
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconResId: String
)

/**
 * Game session model
 */
data class GameSession(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val players: List<Player>,
    val categories: List<Category>,
    val challenges: List<Challenge>,
    val turns: List<Turn>,
    val spotlightMoments: List<SpotlightMoment>
)

data class Player(
    val id: String,
    val name: String
)

data class Category(
    val id: String,
    val name: String
)

data class Challenge(
    val id: String,
    val category: String,
    val type: String, // "truth" or "dare"
    val text: String,
    val playerId: String,
    val completed: Boolean,
    val pointsEarned: Int,
    val timestamp: Long,
    val duration: Long,
    val responseTime: Long
)

data class Turn(
    val id: String,
    val playerId: String,
    val challengeId: String,
    val timestamp: Long
)

/**
 * Repository interface
 */
interface GameSessionRepository {
    fun getGameSession(gameSessionId: String): GameSession
    fun getCurrentGameSession(): GameSession
    fun updateGameSession(gameSession: GameSession)
}