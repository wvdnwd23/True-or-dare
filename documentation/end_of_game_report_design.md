# End-of-Game Report Enhancement Design

## Overview
The End-of-Game Report feature provides players with a comprehensive summary of their gameplay experience, highlighting key statistics, memorable moments, and achievements. The enhanced version will focus on providing detailed category statistics to give players insights into their gameplay preferences and patterns.

## Feature Goals
1. Provide players with detailed statistics about their category usage and performance
2. Create a visually engaging and informative end-of-game experience
3. Encourage replay by highlighting achievements and suggesting new categories to explore
4. Offer shareable content that players can post on social media

## Current End-of-Game Report
The current end-of-game report includes:
- Basic game statistics (duration, number of turns, number of players)
- Player rankings based on points
- List of completed challenges
- Basic achievements

## Enhanced End-of-Game Report

### 1. Category Statistics Section
This new section will provide detailed insights into category usage and performance:

#### Category Usage Breakdown
- Visual representation (pie chart) showing the distribution of categories played
- Percentage and count for each category
- Comparison to previous games (if available)
- Most and least played categories

#### Category Performance Metrics
- Completion rate for each category (percentage of challenges completed)
- Average points earned per category
- Player preferences (which players excelled in which categories)
- Category streaks (consecutive successful challenges in the same category)

#### Category Insights
- Personalized insights based on category usage patterns
- Suggestions for unexplored or underutilized categories
- Compatibility analysis for group play (which categories worked best for this group)

### 2. Visual Design Elements

#### Category Statistics Card
- Clean, modern card design for each category
- Category icon and color coding
- Key statistics prominently displayed
- Progress bars for completion rates
- Micro-animations for interactive elements

#### Data Visualization Components
- Pie charts for category distribution
- Bar charts for comparative performance
- Progress indicators for completion rates
- Heat maps for player-category affinity

#### Achievement Badges
- Special badges for category mastery
- Milestone achievements for category exploration
- Group achievements for category diversity

### 3. Interactive Elements
- Expandable sections for detailed category statistics
- Filters to view statistics by player, category, or challenge type
- Toggles to compare with previous games
- Share buttons for social media posting

## Data Collection Mechanism

### Game Session Tracking
The system will track the following data during gameplay:

#### Category-Level Data
- Number of challenges from each category
- Completion status of each challenge
- Points earned per category
- Time spent on challenges by category
- Player responses to category challenges

#### Player-Category Interaction
- Individual player performance by category
- Player preferences (skip patterns, completion rates)
- Player improvement over time within categories
- Player-specific category achievements

#### Group Dynamics
- Group performance across categories
- Category selection patterns
- Category completion rates as a group
- Category-based social dynamics (who challenges whom with what categories)

### Data Storage
- Category statistics will be stored in the GameSessionEntity
- Historical data will be maintained for trend analysis
- Aggregated statistics will be updated after each game

## Implementation Details

### Data Structure
```kotlin
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

data class GameSessionStatistics(
    val gameId: String,
    val duration: Long,
    val totalTurns: Int,
    val categoryStatistics: Map<String, CategoryStatistics>,
    val playerStatistics: Map<String, PlayerStatistics>,
    val spotlightMoments: Int,
    val achievements: List<Achievement>
)
```

### Statistics Calculation
```kotlin
fun calculateCategoryStatistics(gameSession: GameSession): Map<String, CategoryStatistics> {
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
```

## User Experience Considerations

### Report Flow
1. Game completion animation
2. Overall game statistics summary
3. Player rankings and achievements
4. Category statistics section (new)
5. Memorable moments recap
6. Call to action (play again, try new categories)

### Accessibility Considerations
- Color-blind friendly visualizations with patterns and labels
- Text alternatives for all charts and graphs
- Scalable text for readability
- High contrast mode option

### Performance Optimization
- Lazy loading of statistics sections
- Pre-calculation of statistics during gameplay
- Efficient data storage and retrieval
- Optimized rendering of visualization components

## Testing and Validation
1. Verify accuracy of category statistics calculations
2. Test with various game scenarios (short games, long games, different player counts)
3. Validate visualization rendering on different screen sizes
4. Measure performance impact of statistics tracking during gameplay
5. Gather user feedback on report clarity and usefulness

## Future Enhancements
1. Personalized category recommendations based on play history
2. Social sharing of category statistics with customizable graphics
3. Category challenges and goals for future games
4. Integration with global leaderboards for category mastery
5. Category-based tournaments and special events