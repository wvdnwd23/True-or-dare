# Spotlight Moments Feature Design

## Overview
The Spotlight Moments feature is designed to highlight special or significant moments during gameplay, creating memorable experiences for players. These moments add an extra layer of excitement and engagement to the Truth or Dare game.

## Feature Goals
1. Create memorable gameplay moments that stand out from regular turns
2. Add variety and unpredictability to the game flow
3. Encourage player engagement and excitement
4. Provide special rewards or challenges at key moments

## Spotlight Moment Types

### 1. Milestone Moments
- Triggered when reaching specific game milestones (e.g., every 10th turn, halfway point, final round)
- Provides special challenges or rewards to mark progress
- Examples:
  * "10th Turn Spotlight: The player must answer a truth AND complete a dare"
  * "Halfway Point Spotlight: All players vote on who has been most entertaining so far"

### 2. Random Surprise Moments
- Triggered randomly with a predefined probability (e.g., 10% chance per turn)
- Creates unexpected twists in gameplay
- Examples:
  * "Surprise Spotlight: The current player can choose any other player to complete their turn instead"
  * "Surprise Spotlight: The current player gets to create their own truth or dare for another player"

### 3. Special Category Moments
- Triggered when specific category combinations are selected
- Creates thematic special moments
- Examples:
  * "Hot Category Spotlight: The next three turns must all be dares"
  * "Deep Past + Self-Discovery Spotlight: All players share a brief childhood memory"

### 4. Comeback Moments
- Triggered for players who haven't been active or successful in recent rounds
- Helps balance gameplay and keep all players engaged
- Examples:
  * "Comeback Spotlight: Player who hasn't had a turn in a while gets a special power for their next turn"
  * "Redemption Spotlight: Player who skipped the last challenge gets a second chance with a bonus reward"

## Visual Design Elements

### Spotlight Animation
- Brief animation effect when a Spotlight Moment is triggered
- Bright spotlight effect focusing on the current player
- Particle effects or confetti animation to create excitement
- Sound effect to draw attention to the special moment

### Spotlight Card Design
- Distinctive card design that stands out from regular truth/dare cards
- Gold/silver border or background
- Special icon or badge indicating the type of Spotlight Moment
- Animation when the card is revealed

### UI Elements
- Spotlight counter or indicator showing progress toward next milestone moment
- Special sound effects for Spotlight Moments
- Visual feedback when completing Spotlight challenges

## Implementation Details

### Spotlight Moment Detection Algorithm
1. **Milestone Detection**:
   - Track turn count and player count
   - Trigger at mathematically significant points (10th turn, halfway point, etc.)

2. **Random Moment Generation**:
   - Use weighted random number generator
   - Configurable probability settings
   - Cooldown period to prevent too many spotlight moments in succession

3. **Category-Based Detection**:
   - Monitor selected categories and combinations
   - Trigger on specific combinations or sequences

4. **Player Engagement Monitoring**:
   - Track each player's activity and success rate
   - Identify players who need engagement boosts

### Spotlight Moment Content Generation
1. Create a database of spotlight moment challenges and rewards
2. Categorize by type, intensity, and player count
3. Ensure appropriate content for each game context
4. Include dynamic content that references specific players or previous game events

### Integration with Game Flow
1. Spotlight moment check occurs at the beginning of each turn
2. If triggered, the spotlight moment replaces or enhances the normal turn
3. After completion, game returns to normal flow
4. Results of spotlight moments may affect subsequent gameplay

## User Experience Considerations

### Frequency and Timing
- Spotlight moments should be rare enough to feel special (approximately 10-15% of turns)
- Ensure even distribution throughout gameplay
- Prevent spotlight moments from occurring on consecutive turns

### Player Feedback
- Clear explanation of what triggered the spotlight moment
- Simple instructions for special rules or challenges
- Visual and audio feedback for successful completion
- Celebration of spotlight moment achievements

### Customization Options
- Allow players to adjust spotlight moment frequency
- Option to enable/disable specific types of spotlight moments
- Custom spotlight moments for special occasions (birthdays, holidays, etc.)

## Technical Implementation

### Data Structure
```kotlin
data class SpotlightMoment(
    val id: String,
    val type: SpotlightType,
    val title: String,
    val description: String,
    val challenge: String,
    val reward: String?,
    val visualEffect: VisualEffectType,
    val soundEffect: SoundEffectType,
    val applicableCategories: List<String>?
)

enum class SpotlightType {
    MILESTONE,
    RANDOM,
    CATEGORY_BASED,
    COMEBACK
}

enum class VisualEffectType {
    CONFETTI,
    SPOTLIGHT,
    GLOW,
    FIREWORKS,
    PULSE
}

enum class SoundEffectType {
    FANFARE,
    DRUMROLL,
    CHIME,
    APPLAUSE,
    SURPRISE
}
```

### Detection Logic
```kotlin
fun checkForSpotlightMoment(gameState: GameState): SpotlightMoment? {
    // Check for milestone moments
    if (isMilestoneTurn(gameState.currentTurn)) {
        return selectSpotlightMoment(SpotlightType.MILESTONE)
    }
    
    // Check for category-based moments
    if (hasSpecialCategoryCombo(gameState.selectedCategories)) {
        return selectSpotlightMoment(SpotlightType.CATEGORY_BASED)
    }
    
    // Check for comeback opportunities
    val inactivePlayer = findInactivePlayer(gameState.players)
    if (inactivePlayer != null && gameState.currentPlayer == inactivePlayer) {
        return selectSpotlightMoment(SpotlightType.COMEBACK)
    }
    
    // Random chance for surprise moments
    if (shouldTriggerRandomMoment()) {
        return selectSpotlightMoment(SpotlightType.RANDOM)
    }
    
    return null
}
```

## Testing and Validation
1. Playtest with various player counts and game durations
2. Verify spotlight moment frequency feels appropriate
3. Test all spotlight moment types in different game contexts
4. Gather feedback on which spotlight moments are most enjoyable
5. Ensure spotlight moments enhance rather than disrupt gameplay flow

## Future Enhancements
1. Player-created spotlight moments
2. Themed spotlight moments for holidays or special events
3. Achievement system tied to spotlight moment completion
4. Multi-player spotlight challenges requiring cooperation
5. Progressive spotlight moments that build on previous ones