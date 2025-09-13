# Truth or Dare - Session Experience Features

## Overview

This document outlines the enhanced session experience features for the Truth or Dare game, including Mix Mode, Progressive Mode, Spotlight Moments, and End-of-Game Report. These features are designed to create a more engaging, dynamic, and memorable gameplay experience.

## Mix Mode

Mix Mode randomly alternates across selected categories and sub-options to create a varied and unpredictable gameplay experience.

### Implementation Details

1. **Selection Pool Creation**:
   - When Mix Mode is enabled, the system creates a pool of all items from selected categories and sub-options
   - If specific sub-options are selected, only items matching those sub-options are included
   - If no sub-options are selected for a category, all items from that category are included

2. **Randomization Algorithm**:
   - Items are shuffled using a weighted randomization algorithm
   - The algorithm ensures balanced distribution across categories and sub-options
   - Truth/Dare balance is maintained at approximately 50/50 (±10%)

3. **Category Tracking**:
   - The system tracks which categories and sub-options have been recently used
   - Avoids serving multiple consecutive items from the same category/sub-option
   - Ensures all selected categories appear before repeating

4. **User Interface**:
   - Current category is visually indicated during gameplay
   - Small icon/badge shows which category the current question/dare belongs to
   - Animation between different categories creates a sense of variety

5. **Customization Options**:
   - Users can adjust category weighting (e.g., "More Party, Less Deep")
   - Option to set minimum/maximum consecutive items from same category

## Progressive Mode

Progressive Mode starts with softer, lighter content and gradually increases in intensity throughout the gameplay session.

### Implementation Details

1. **Intensity Progression**:
   - Game begins with items labeled as "Soft" intensity
   - Gradually introduces "Medium" intensity items after a set number of rounds
   - Eventually introduces "Hot" intensity items (if age-appropriate and selected)

2. **Progression Algorithm**:
   - Default progression: 10 Soft → 15 Medium → Hot (remainder of game)
   - Customizable progression speed (Slow, Normal, Fast)
   - Option to cap maximum intensity (e.g., never exceed Medium)

3. **Category Progression**:
   - Begins with lighter categories (Soft, Icebreaker, Silly)
   - Gradually introduces deeper categories (Personal, Self-Discovery)
   - Sensitive categories (Mental Health, Trauma & Healing) only appear later in the session

4. **Visual Indicators**:
   - Progress bar shows current intensity level
   - Notifications when advancing to next intensity level
   - Color scheme shifts subtly as intensity increases

5. **Override Options**:
   - "Boost" button to immediately jump to higher intensity
   - "Cool down" button to temporarily return to lower intensity
   - Option to lock current intensity level

## Spotlight Moments

Spotlight Moments are special prompts that create memorable highlights during gameplay.

### Implementation Details

1. **Spotlight Types**:
   - **Best Question**: Particularly thought-provoking or entertaining question
   - **Group Challenge**: Activity involving all players simultaneously
   - **Callback**: References earlier response from the session
   - **Superlative**: "Most likely to..." style questions
   - **Confession**: Deeper, more revealing prompts

2. **Triggering Mechanism**:
   - Appears approximately every 10-15 regular turns
   - More frequent in longer sessions
   - Can be triggered by specific in-game events (e.g., multiple skips)

3. **Visual Treatment**:
   - Special animation and sound effect
   - Distinct card design with spotlight visual effect
   - Timer extension for these special moments

4. **Rewards & Recognition**:
   - Option to save favorite Spotlight Moments
   - Special achievements for completing Spotlight challenges
   - Featured in End-of-Game Report

5. **Customization**:
   - Adjust frequency of Spotlight Moments
   - Focus on specific types of Spotlight Moments
   - Option to disable specific Spotlight types

## End-of-Game Report

The End-of-Game Report provides a summary of the gameplay session, highlighting statistics, memorable moments, and player participation.

### Implementation Details

1. **Statistics Tracking**:
   - **Category Distribution**: Percentage of questions from each category
   - **Sub-Option Breakdown**: Count per sub-option
   - **Truth/Dare Balance**: Ratio of truths vs. dares played
   - **Intensity Progression**: How intensity changed throughout the session
   - **Player Participation**: Equal distribution of turns (if player names entered)

2. **Notable Moments**:
   - Record of all Spotlight Moments
   - Option to mark favorite questions/dares during gameplay
   - "Most Skipped" category or question type
   - "Most Engaged" category (based on time spent or discussion)

3. **Visualization**:
   - Colorful charts showing category distribution
   - Timeline of intensity progression
   - Word cloud of themes or keywords
   - Infographic-style presentation of statistics

4. **Sharing & Saving**:
   - Option to save report to device
   - Share report with players (with consent)
   - Export favorite questions/dares for future sessions
   - Privacy controls for sensitive content

5. **Feedback Collection**:
   - Rating system for the overall session
   - Feedback on category enjoyment
   - Suggestions for future sessions
   - Report inappropriate content

## Integration Between Features

### Mix Mode + Progressive Mode

When both Mix Mode and Progressive Mode are enabled:

1. The system creates separate pools for each intensity level
2. Randomization occurs within the current intensity level pool
3. As intensity progresses, new pools become available
4. Category mixing still occurs within each intensity level

### Spotlight Moments in Different Modes

1. **In Mix Mode**: Spotlight Moments draw from all selected categories
2. **In Progressive Mode**: Spotlight Moments match current intensity level
3. **In Standard Mode**: Spotlight Moments follow regular category selection

### End-of-Game Report Customization

The End-of-Game Report adapts based on which features were active:

1. **Mix Mode Report**: Shows category distribution and balance
2. **Progressive Mode Report**: Shows intensity progression timeline
3. **Standard Mode Report**: Focuses on player participation and favorites

## Implementation Checklist

- [ ] Mix Mode randomization algorithm
- [ ] Progressive Mode intensity progression system
- [ ] Spotlight Moments content and triggering mechanism
- [ ] End-of-Game Report statistics tracking
- [ ] User interface for mode selection
- [ ] Settings for customizing each feature
- [ ] Integration between different modes
- [ ] Testing across various game configurations