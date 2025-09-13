# Truth or Dare Deluxe - Comprehensive Overview

## Introduction

Truth or Dare Deluxe is a modern, feature-rich Android application built with Kotlin and Jetpack Compose. It offers a completely offline Truth or Dare game experience with advanced features like voice recognition, adaptive learning, and multiple game modes. The app is designed with privacy in mind and requires no internet permissions.

## Core Features

### 1. Offline Operation
- No internet permissions required
- All functionality works completely offline
- Privacy-focused design

### 2. Voice Recognition
- Uses Vosk for Dutch language voice recognition
- Processes speech locally on the device
- Enables hands-free gameplay

### 3. Adaptive Learning
- Learns user preferences over time
- Adjusts question selection based on player history
- Personalizes the game experience

### 4. Multiple Game Modes
- **Classic Mode**: Traditional Truth or Dare gameplay
- **Drink Mode**: Adult-oriented version with drinking rules
- **Party Mode**: Group-focused with fun, crazy dares
- **Couples Mode**: Romantic and intimate questions for couples
- **Family Mode**: Kid-friendly content for family game nights
- **Teen Mode**: Age-appropriate content for teenagers
- **Hardcore Mode**: Extreme, adults-only content

### 5. Content Filtering
- Content filter levels (LOW, MEDIUM, HIGH, EXTREME)
- Age-appropriate content selection
- Safety checks for sensitive content

### 6. Internationalization
- Support for multiple languages:
  - English
  - Dutch
  - German
  - French
  - Spanish

### 7. Extensive Question Database
- 12,000+ questions across 12 categories
- Question generation system for endless variety
- Follow-up questions based on answers

## Technical Architecture

### 1. App Structure
The app follows a modern Android architecture with clear separation of concerns:

```
app/
├── src/
│   ├── main/
│   │   ├── assets/         # Voice models, question templates, sounds
│   │   ├── java/
│   │   │   └── com/wes/truthdare/
│   │   │       ├── buildtasks/      # Build-time tasks (question generation)
│   │   │       ├── core/            # Core functionality
│   │   │       │   ├── agents/      # Core functional interfaces
│   │   │       │   ├── asr/         # Automatic Speech Recognition
│   │   │       │   ├── data/        # Data models and storage
│   │   │       │   ├── di/          # Dependency injection
│   │   │       │   ├── impl/        # Implementation of agents
│   │   │       │   ├── nlp/         # Natural Language Processing
│   │   │       │   ├── report/      # Reporting functionality
│   │   │       │   ├── security/    # Security features
│   │   │       │   ├── selector/    # Question selection logic
│   │   │       │   └── util/        # Utility classes
│   │   │       └── ui/              # User interface
│   │   │           ├── components/  # Reusable UI components
│   │   │           ├── navigation/  # Navigation system
│   │   │           ├── screens/     # App screens
│   │   │           └── theme/       # App theming
│   │   └── res/             # Resources (strings, drawables, etc.)
│   └── test/                # Unit tests
```

### 2. Core Components (Agents)

The app uses an agent-based architecture with specialized components:

- **VoiceAgent**: Handles speech recognition using Vosk
- **EmotionAgent**: Analyzes emotional content in speech
- **QuestionAgent**: Selects appropriate questions
- **StoryAgent**: Tracks game narrative and flow
- **SafetyAgent**: Handles content moderation
- **LearningAgent**: Adapts to user preferences

### 3. Data Architecture

- **Room Database**: Local storage for game data
- **Entities**:
  - PlayerEntity: Represents game players
  - GameSessionEntity: Tracks game sessions
  - QuestionHistoryEntity: Records question history
  - PlayerPreferenceEntity: Stores player preferences
  - PlayerStatisticsEntity: Tracks player statistics
  - JournalEntryEntity: Stores reflections and notes

- **Data Models**:
  - EnhancedQuestion: Rich question model with metadata
  - SessionLog: Complete record of a game session
  - Various supporting models for game functionality

### 4. UI Architecture

- **Jetpack Compose**: Modern declarative UI
- **MVVM Pattern**: Clear separation of UI and business logic
- **Navigation Component**: Handles screen transitions
- **Material Design 3**: Modern visual styling

## Game Modes in Detail

### 1. Classic Mode
- Traditional Truth or Dare gameplay
- Mix of fun and challenging questions
- Medium content filter level (suitable for 16+)

### 2. Drink Mode
- Adult-oriented drinking game (18+)
- Features:
  - Shot counter to track drinks
  - Lie cards system (3 per player)
  - 40+ drinking mini-games
  - Responsible drinking features:
    - Water reminders
    - Designated driver selection
    - Session time/drink limits
    - Emergency contacts
  - Drinking rules enforcement

### 3. Party Mode
- Fun, crazy, group-based dares
- Perfect for livening up gatherings
- Medium content filter level (suitable for 16+)

### 4. Couples Mode
- Romantic and intimate questions
- Designed for couples to enjoy together
- High content filter level (suitable for 18+)

### 5. Family Mode
- Safe, kid-friendly content
- Suitable for family game nights
- Low content filter level (suitable for 8+)

### 6. Teen Mode
- Age-appropriate questions and dares
- Suitable for teenagers
- Medium content filter level (suitable for 13+)

### 7. Hardcore Mode
- Extreme, adults-only content
- For the most daring players
- Extreme content filter level (strictly 18+)

## User Flow

1. **Onboarding**: First-time setup and introduction
2. **Player Selection**: Choose and manage players
3. **Game Mode Selection**: Select desired game mode
4. **Categories Selection**: Choose question categories
5. **Game Screen**: Main gameplay with questions/dares
6. **Reflection**: Post-game summary and highlights
7. **Reports**: View statistics and session history

## Question System

### Question Types
- **Truth**: Questions requiring honest answers
- **Dare**: Challenges requiring physical actions

### Question Targeting
- **Single**: Directed at one player
- **Group**: Directed at the whole group
- **Pair**: Directed at a pair of players
- **Self**: Directed at the current player

### Question Difficulty Levels
- **Easy**: Simple, light-hearted questions
- **Medium**: Moderately challenging questions
- **Hard**: Difficult or more personal questions
- **Extreme**: Very challenging or intimate questions

### Question Selection Logic
- Uses player history to avoid repetition
- Considers player preferences and comfort levels
- Implements safety checks for sensitive content
- Provides follow-up questions based on answers
- Uses "star tags" for themed question sequences

## Responsible Features

### Drink Mode Safety
- Water reminder system
- Designated driver selection
- Session time and drink limits
- Emergency contacts access
- Responsible drinking reminders

### Content Safety
- Age-appropriate content filtering
- Safety agent to screen sensitive topics
- Trigger detection for potentially upsetting content
- Option to skip uncomfortable questions

## Future Development Plans

According to the upgrade plan, future enhancements include:

1. **UI/UX Overhaul**:
   - Full Material Design 3.0 implementation
   - Animated dare cards with haptic feedback
   - Interactive sounds and background music
   - Spin-the-bottle mechanic

2. **Expanded Internationalization**:
   - Additional languages (Italian, Portuguese, Turkish)
   - Automatic language detection

3. **Monetization Strategy**:
   - AdMob integration
   - Premium subscription option
   - Exclusive content for subscribers

4. **Technical Optimizations**:
   - Firebase Analytics integration
   - Error logging system
   - Performance improvements

## Technical Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Minimum Android version: API 28 (Android 9 Pie)

## Conclusion

Truth or Dare Deluxe represents a modern, feature-rich approach to the classic party game. With its multiple game modes, adaptive learning, voice recognition, and focus on responsible gaming, it offers a comprehensive and engaging experience for various age groups and social settings.