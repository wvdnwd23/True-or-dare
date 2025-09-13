# Truth or Dare Deluxe App - Complete Specification

## Overview
Create a comprehensive Truth or Dare mobile application called "Truth or Dare Deluxe" with multiple game modes, extensive question libraries, and special features like Drink Mode. The app should be engaging, customizable, and suitable for various social settings.

## Core Features

### 1. Game Modes
- **Classic Mode**: Traditional truth or dare gameplay
- **Drink Mode**: Party-oriented version with drinking rules
- **Couples Mode**: Romantic questions and dares for couples
- **Family Mode**: Clean content suitable for family gatherings
- **Party Mode**: High-energy questions and dares for social gatherings
- **Deep Talk Mode**: Thought-provoking questions for meaningful conversations

### 2. Question Libraries
- **12 Categories** with 1000 questions each:
  - Casual
  - Party
  - Deep
  - Romantic
  - Family
  - Friends
  - Funny
  - Challenge
  - Personal
  - Childhood
  - Future
  - Hypothetical
- **Drink Mode**: 100 specialized drinking game questions
- **Question Structure**:
  ```json
  {
    "id": "unique-id",
    "type": "truth|dare",
    "category": "category-name",
    "targets": "single|group|pair|self",
    "depthLevel": 1-5,
    "tags": ["tag1", "tag2", "..."],
    "text": "Question text"
  }
  ```

### 3. Player Management
- Player profiles with customizable names and avatars
- Player turn tracking
- Player preference tracking (question types they enjoy)
- Session history
- Designated driver feature for Drink Mode

### 4. Drink Mode Special Features
- **Shot Counter**: Track drinks consumed with increment/decrement buttons
- **Lie Cards**: Each player gets 3 lie cards to avoid answering truthfully
- **Mini-Games**: 40 drinking mini-games with difficulty levels:
  - Easy (14 games)
  - Medium (14 games)
  - Hard (12 games)
- **Drinking Rules**: Customizable rules for the game session
- **Responsible Drinking Features**:
  - Water reminder system (every 20 minutes)
  - Designated driver selection
  - Session time limits
  - Drink limits
  - Emergency contacts

### 5. UI Components
- **Main Screen**: Game mode selection
- **Player Setup Screen**: Add/edit players
- **Game Screen**: Question display, truth/dare selection
- **Drink Mode Screen**:
  - Shot counter
  - Lie card tracker
  - Mini-game selector
  - Drinking rules display
- **Settings Screen**: Language, sound, content filters

### 6. Accessibility & Customization
- **Multi-language Support**:
  - English
  - Dutch
  - German
  - Spanish
  - French
- **Content Filters**: Age-appropriate content settings
- **Dark Mode**: Full dark mode support
- **Sound Effects**: Optional sound effects for actions

## Technical Requirements

### 1. Architecture
- **MVVM Architecture**:
  - Models for data entities (questions, players, game sessions)
  - ViewModels for screen logic
  - Composable UI components
- **Repository Pattern** for data access
- **Dependency Injection** using Hilt

### 2. Data Management
- **Local Storage**:
  - Room Database for persistent data
  - DataStore for preferences
- **Question Generation**:
  - Template-based question generation system
  - Category-specific templates and synonyms

### 3. UI Implementation
- **Modern UI** with Material Design 3
- **Jetpack Compose** for all UI components
- **Animations** for transitions and interactions
- **Responsive Design** for different screen sizes

### 4. Special Features Implementation
- **Mini-Game System**:
  ```kotlin
  data class MiniGame(
      val title: String,
      val description: String,
      val difficulty: Difficulty
  )
  
  enum class Difficulty {
      EASY, MEDIUM, HARD
  }
  ```

- **Drinking Rules System**:
  ```kotlin
  data class DrinkingRule(
      val title: String,
      val description: String
  )
  ```

- **Water Reminder System**:
  ```kotlin
  class WaterReminderManager {
      fun startReminders(intervalMinutes: Int = 20)
      fun stopReminders()
      fun dismissAndRemindLater()
      fun dismissReminder()
  }
  ```

- **Session Limits Manager**:
  ```kotlin
  class SessionLimitsManager {
      fun startSession()
      fun endSession()
      fun setTimeLimit(minutes: Int?)
      fun setDrinkLimit(count: Int?)
      fun clearLimits()
      fun getRemainingTimeMinutes(): Flow<Int?>
      fun isTimeLimitReached(): Flow<Boolean>
  }
  ```

### 5. Safety Features
- **Designated Driver Manager**:
  ```kotlin
  class DesignatedDriverManager {
      fun setDesignatedDriver(playerId: String?)
      fun clearDesignatedDriver()
      fun isDesignatedDriver(playerId: String): Flow<Boolean>
  }
  ```

- **Emergency Contacts**:
  ```kotlin
  data class EmergencyContact(
      val id: String,
      val name: String,
      val phoneNumber: String,
      val relationship: String
  )
  ```

## Mini-Games List (40 Total)

### Easy Difficulty (14 games)
1. **Never Have I Ever**: Players take turns saying 'Never have I ever...' followed by something they've never done. Anyone who has done it takes a drink.
2. **Most Likely To**: Someone asks 'Who's most likely to...?' Everyone points at who they think is most likely. Person with most points drinks that many shots.
3. **Waterfall**: Everyone starts drinking at the same time. No one can stop until the person to their right stops.
4. **Thumb Master**: Designated player can put their thumb on the table anytime. Last player to notice and put their thumb down drinks.
5. **Medusa**: Everyone looks down. On count of 3, look up at someone. If they're looking at you too, both drink.
6. **Coin Flip**: Predict heads or tails. If wrong, drink. If right, give out a drink.
7. **Staring Contest**: Two players stare at each other. First to blink or laugh drinks.
8. **Rock Paper Scissors**: Loser of each round drinks. Play best of 3 or 5.
9. **Word Scramble**: Unscramble drinking-related words. Last to solve drinks.
10. **Truth Chain**: Each player adds a 'truth' to a growing sentence. First to hesitate or repeat a word drinks.
11. **Dare Roulette**: Spin a virtual wheel to determine a random dare. Refuse and drink.
12. **Tongue Twisters**: Say a tongue twister three times fast. Mess up and drink.
13. **Counting Game**: Count from 1, replacing certain numbers with words. Mess up and drink.
14. **Accent Challenge**: Say a phrase in a specific accent. Group votes if it's good enough or drink.

### Medium Difficulty (14 games)
1. **Categories**: Choose a category (e.g., car brands). Go around the circle naming items in that category. First to hesitate or repeat takes a shot.
2. **Word Association**: First player says a word, next player must say a related word. Continue until someone hesitates or repeats a word.
3. **Kings Cup**: Place a cup in the center surrounded by a circle of cards. Take turns drawing cards, each with a different rule.
4. **Quarters**: Bounce a quarter into a shot glass. If you make it, choose someone to drink.
5. **Straight Face**: Players take turns trying to make others laugh while they try to keep a straight face. First to laugh drinks.
6. **Rhyme Time**: First player says a word, next player must say a word that rhymes. Continue until someone can't think of a rhyme.
7. **Alphabet Game**: Choose a category. First player says something in that category starting with A, next with B, and so on.
8. **Drink Trivia**: Answer alcohol-related trivia questions. Wrong answer means drink.
9. **Song Lyrics**: Continue the lyrics when the song stops. Fail and drink.
10. **Movie Quotes**: Identify the movie from a famous quote. Wrong answer means drink.
11. **Two Truths, One Lie**: Tell two truths and one lie. If others guess the lie, you drink. If they can't identify the lie, they drink.
12. **Drink Jenga**: Take turns removing blocks from a tower. Topple it and drink. Some blocks have special rules written on them.
13. **Celebrity**: Describe a celebrity without saying their name. Fail to get others to guess and drink.
14. **Drink Dice**: Roll two dice. If you roll doubles, give out drinks equal to the sum. If you roll a 7, take a drink yourself.

### Hard Difficulty (12 games)
1. **Flip Cup**: Divide into two teams. Each player drinks and then tries to flip their cup upside down. First team to finish wins.
2. **Beer Pong**: Set up cups in a triangle at each end of a table. Take turns throwing ping pong balls into opponent's cups. If it lands, they drink.
3. **Ride the Bus**: A multi-round card game where players guess card values. Wrong guesses mean drinks.
4. **Buzz**: Count around the circle, replacing any number containing 7 or divisible by 7 with 'buzz'. Mess up and drink.
5. **Chandelier**: Center cup surrounded by outer cups. Land ball in center, everyone drinks. Land in outer cup, assign that drink.
6. **Speed Bartender**: Name as many cocktails as possible in 30 seconds. Lowest count drinks.
7. **Drink Pong Variations**: Special rules for beer pong with added challenges like using non-dominant hand, standing on one foot, or eyes closed.
8. **Flip Cup Tournament**: Tournament-style flip cup with elimination rounds. Losers of each round drink and are eliminated until one winner remains.
9. **Shot Roulette**: Spin to determine who takes a shot from a selection of mixed drinks. Some drinks might be non-alcoholic, others very strong.
10. **Power Hour**: Take a small drink every minute for an hour (simulated in-game). Players who miss their minute must take a double drink.
11. **Stack Attack**: Stack cups in a specific pattern while timed. Fail and drink. Patterns get increasingly complex with each round.
12. **Drinking Rules Challenge**: Players must remember and follow an increasing number of arbitrary drinking rules. Breaking a rule means drinking.

## Drinking Rules (Default Set)
1. **Truth Penalty**: If you refuse to answer a Truth, take 2 shots.
2. **Dare Penalty**: If you refuse to perform a Dare, take 3 shots.
3. **Lie Card**: Each player starts with 3 Lie Cards. Use one to avoid answering truthfully.
4. **Caught Lying**: If caught lying without using a Lie Card, take 4 shots.
5. **Mini-Game Loser**: The loser of a mini-game takes 1 shot.
6. **Phone Check**: If you check your phone during the game, take 1 shot.
7. **Name Slip**: If you say someone's name instead of 'the person to my left/right', take 1 shot.
8. **Bathroom Break**: Take 1 shot if you need a bathroom break (except for emergencies).
9. **Laugh Control**: If you laugh during a serious truth question, take 1 shot.
10. **Drink Spill**: If you spill your drink, take 2 shots as penalty.
11. **Hydration Rule**: Drink a glass of water after every 3 alcoholic drinks.
12. **Designated Driver**: The designated driver doesn't drink alcohol and gets to assign 3 shots during the game.

## Responsible Drinking Features
1. **Water Reminder**: Periodic reminders to drink water (default: every 20 minutes)
2. **Designated Driver**: Select a player who won't drink and can drive others home
3. **Session Limits**:
   - Time limit (e.g., stop after 2 hours)
   - Drink limit (e.g., maximum 5 drinks per person)
4. **Emergency Contacts**: Quick access to emergency contacts and ride services
5. **Responsible Drinking Tips**: Educational content about alcohol consumption

## UI Screens and Components

### 1. Main Screen
- App logo and title
- Game mode selection buttons (Classic, Drink, Couples, Family, Party, Deep Talk)
- Settings button
- Player management button

### 2. Player Setup Screen
- Add player button
- Player list with avatars and names
- Edit/delete player options
- Start game button

### 3. Game Screen
- Current player indicator
- Question display area
- Truth/Dare selection buttons
- Next player button
- Game statistics (questions answered, rounds played)

### 4. Drink Mode Screen
- Shot counter with +/- buttons
- Lie cards remaining indicator
- Mini-game section with random generator
- Drinking rules display
- Responsible drinking banner

### 5. Settings Screen
- Language selection
- Sound effects toggle
- Content filter settings (age-appropriate content)
- Dark mode toggle
- About section with credits

### 6. Dialogs and Modals
- **Game Instructions Dialog**: Explains selected mini-game rules
- **Difficulty Filter Dialog**: Filters mini-games by difficulty
- **Water Reminder Dialog**: Reminds players to drink water
- **Session Limits Dialog**: Set time and drink limits
- **Emergency Contacts Dialog**: Display and manage emergency contacts

## Implementation Guidelines

### 1. Project Structure
```
app/
├── src/
│   ├── main/
│       ├── java/com/wes/truthdare/
│       │   ├── TruthDareApp.kt
│       │   ├── buildtasks/
│       │   │   ├── GenerateQuestions.kt
│       │   │   └── QuestionTemplateEngine.kt
│       │   ├── core/
│       │   │   ├── agents/
│       │   │   ├── data/
│       │   │   ├── di/
│       │   │   ├── impl/
│       │   │   ├── nlp/
│       │   │   ├── security/
│       │   │   ├── selector/
│       │   │   └── util/
│       │   └── ui/
│       │       ├── components/
│       │       ├── navigation/
│       │       ├── screens/
│       │       └── theme/
│       ├── assets/
│       │   ├── questions/
│       │   ├── sounds/
│       │   └── templates/
│       └── res/
│           ├── drawable/
│           ├── values/
│           ├── values-de/
│           ├── values-en/
│           ├── values-es/
│           ├── values-fr/
│           └── values-nl/
```

### 2. Build Process
1. Generate question packs during build process
2. Compile with Kotlin compiler
3. Package resources and assets
4. Sign APK with release keystore

### 3. Testing Strategy
- Unit tests for ViewModels and repositories
- UI tests for critical user flows
- Manual testing of game mechanics

## Additional Requirements

### 1. Performance
- Fast app startup time (<2 seconds)
- Smooth animations and transitions
- Efficient memory usage

### 2. Security
- Secure storage for user data
- Privacy-focused design

### 3. Accessibility
- Support for screen readers
- Configurable text sizes
- High contrast mode

### 4. Future Expansion
- Design with extensibility in mind for:
  - New game modes
  - Additional languages
  - More mini-games
  - Online multiplayer

## Deliverables
1. Complete source code
2. Compiled APK file
3. Documentation for code and architecture
4. User guide