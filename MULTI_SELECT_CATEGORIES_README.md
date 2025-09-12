# Multi-Select Categories Feature

## Overview

The Multi-Select Categories feature enhances the Truth or Dare Deluxe app by allowing players to select multiple categories and sub-options simultaneously, creating a highly customized game experience. This document provides an overview of the feature, its implementation, and how to use it.

## Features

### 1. Category Selection
- 14 distinct categories covering a wide range of content
- Visual indicators (emojis) and badges (18+, Opt-in) for easy identification
- Multi-select capability to combine different content types

### 2. Sub-Option Selection
- Each category contains 3 sub-options for fine-tuning content
- Expandable category cards reveal sub-options when selected
- Individual selection of sub-options within categories

### 3. Content Filtering
- Intensity slider to control content spiciness (Soft, Medium, Hot)
- Age verification for 18+ content
- Explicit opt-in for sensitive topics (Trauma & Healing, Mental Health)

### 4. Quick Picks
- One-tap selection of popular category combinations:
  - Party Quick Pick: Fun and lively content for parties
  - Deep Quick Pick: Meaningful conversations and self-reflection
  - Couples Quick Pick: Relationship-focused content

### 5. Preview Panel
- Sample questions from selected categories before starting
- Ensures content matches player expectations

## Implementation Details

### Data Structure

The feature is built on these key data models:

1. **Category**
   - Properties: id, name, emoji, description, isAgeRestricted, requiresOptIn
   - Represents a main content category

2. **SubOption**
   - Properties: id, categoryId, name, description
   - Represents a specific theme within a category

3. **EnhancedQuestionV2**
   - Properties: type, text, categoryId, subOptionIds, intensityLevel, etc.
   - Questions tagged with categories, sub-options, and intensity levels

4. **IntensityLevel**
   - Enum: SOFT, MEDIUM, HOT
   - Controls content spiciness

### Content Organization

Content is organized in JSON files with this structure:
```json
{
  "category": {
    "id": "category_id",
    "name": "Category Name",
    "emoji": "🙂",
    "description": "Category description",
    "isAgeRestricted": false,
    "requiresOptIn": false
  },
  "subOptions": [
    {
      "id": "sub_option_id",
      "name": "Sub-Option Name",
      "description": "Sub-option description"
    }
  ],
  "questions": [
    {
      "id": "question_id",
      "type": "TRUTH",
      "text": "Question text",
      "subOptionIds": ["sub_option_id"],
      "targets": "SINGLE",
      "intensityLevel": "SOFT",
      "contentFilterLevel": "LOW"
    }
  ]
}
```

## Categories and Sub-Options

### 1. Soft 🙂
- Icebreakers
- Funny Mini-Dares
- Would You Rather

### 2. Medium 🔶
- Confessions
- Skill Show
- Social Fun

### 3. Hot 18+ 🔥 (Age-restricted)
- Flirty Truths
- Kissing / Touch Dares
- Preference Scale

### 4. Extreme 18+ 🚨 (Age-restricted)
- Roleplay Challenges
- Fantasy Exposure
- Risky Dares

### 5. Couples 💑
- Relationship Memories
- Love Languages
- Future Goals Together

### 6. Couples Hot 💑🔥 (Age-restricted)
- Bedroom Preferences
- Fantasy Exchange
- Intimacy Dares

### 7. Dirty 😏 (Age-restricted)
- NSFW Truths
- Spicy Dares
- Truth-or-Drink

### 8. Deep Past 🌌
- First Times
- Turning Points
- Regrets & Lessons

### 9. Mental Health 🧠 (Opt-in)
- Coping & Stress
- Boundaries
- Honest Check-In

### 10. Childhood 👶
- School Stories
- Family Traditions
- Embarrassing Kid Moments

### 11. Self-Growth 🌱
- Habits & Goals
- Dream Big
- 30-Day Challenges

### 12. Trauma & Healing ❤️‍🩹 (Opt-in)
- Safe Sharing
- Triggers & Comfort
- Consent & Boundaries

### 13. Personal 🧍
- Inner Truths
- Vulnerabilities
- Life-Changing Moments

### 14. Self-Discovery 🌟
- Values & Identity
- Blind Spots
- Future Self

## How to Use

1. **Access the Feature**
   - From the main menu, select "Play"
   - Choose players
   - The multi-select categories screen will appear

2. **Select Categories**
   - Tap on categories to select them
   - Selected categories will be highlighted
   - Age-restricted categories require verification

3. **Select Sub-Options**
   - Tap on a selected category to expand it
   - Choose specific sub-options within the category
   - If no sub-options are selected, all will be included

4. **Adjust Intensity**
   - Use the slider to set content intensity
   - Lower values show only SOFT content
   - Higher values include MEDIUM and HOT content

5. **Use Quick Picks**
   - Tap a Quick Pick button to automatically select a preset combination
   - Modify the selection as needed after applying a Quick Pick

6. **Preview Content**
   - Tap "Preview Selected Content" to see sample questions
   - Ensure the content matches your expectations

7. **Start Game**
   - Tap "Start Game" to begin playing with your selections

## Safety Features

- **Age Verification**: Required for Hot 18+, Extreme 18+, Couples Hot, and Dirty categories
- **Explicit Opt-In**: Required for Trauma & Healing and Mental Health categories
- **Family/Teen Modes**: Automatically hide age-restricted content
- **Skip Option**: Players can always skip questions without penalty
- **Progressive Mode**: Gradually increases intensity during gameplay

## Technical Integration

The multi-select categories feature integrates with the existing app through:

1. **Navigation**: Added to the game flow after player selection
2. **Question Selection**: Enhanced algorithm filters questions based on selections
3. **Game Session**: Updated to store multi-category preferences
4. **End-of-Game Report**: Enhanced to show category statistics

## Future Enhancements

Planned enhancements for this feature include:

1. **Spotlight Moments**: Special prompts highlighting the best moments
2. **Custom Categories**: User-created categories and questions
3. **Favorite Combinations**: Save favorite category combinations
4. **Enhanced Statistics**: More detailed tracking of category usage