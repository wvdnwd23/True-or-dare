# Truth or Dare Deluxe

A completely offline Truth or Dare app built with Kotlin and Jetpack Compose.

## Features

- Completely offline operation - no internet permissions required
- Voice recognition using Vosk for Dutch language
- Follow-up questions based on answers
- Adaptive learning to user preferences
- Privacy-focused design
- Enhanced question system with 4 game modes:
  - Classic mode: 12,000 questions (1000 per category)
  - Couples mode: 12,000 questions (1000 per category)
  - Party mode: 12,000 questions (1000 per category)
  - Drink mode: 1,200 questions (100 per category)

## Game Modes

### Classic Mode
Traditional truth or dare gameplay with a wide variety of questions suitable for all ages and settings.

### Couples Mode
Romantic and relationship-focused questions designed for partners to learn more about each other and strengthen their bond.

### Party Mode
High-energy, fun questions perfect for group settings and social gatherings.

### Drink Mode
Adult-oriented questions with drinking challenges (all with non-alcoholic alternatives). Limited to 100 questions per category to encourage responsible drinking.

## Project Structure

The app follows a modular architecture with the following components:

### Core Components

- **Agents**: Interfaces and implementations for the app's core functionality
  - VoiceAgent: For speech recognition using Vosk
  - EmotionAgent: For analyzing emotional content in speech
  - QuestionAgent: For selecting appropriate questions
  - StoryAgent: For tracking game narrative
  - SafetyAgent: For content moderation
  - LearningAgent: For adapting to user preferences

- **NLP Engine**: Rule-based natural language processing
  - Tagger: For extracting semantic tags from text
  - IntentDetector: For detecting user intent
  - SentimentAnalyzer: For analyzing sentiment
  - TriggerScanner: For detecting sensitive content

- **ASR**: Automatic Speech Recognition
  - AssetUnpacker: For extracting ASR model files
  - VoskVoiceAgent: Implementation of VoiceAgent using Vosk

- **Data**: Database and preferences
  - Room database with entities and DAOs
  - Encrypted storage using androidx.security
  - DataStore for app preferences

- **Selector**: Question selection logic
  - Follow-up questions
  - Star queue for related questions
  - Safety checks

### UI Components

- **Screens**:
  - Onboarding
  - Players selection
  - Categories selection
  - Game screen
  - Settings
  - Reflection/summary

- **Navigation**: NavHost and routes

- **Theme**: Colors, typography, and styles

## Question Generation

The app includes an enhanced question generator that creates:
- 1000 questions per category for Classic, Couples, and Party modes
- 100 questions per category for Drink mode

See [QUESTION_GENERATOR_README.md](QUESTION_GENERATOR_README.md) for details on how to use and customize the question generator.

## Building the Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps

1. Clone the repository
2. Open the project in Android Studio
3. Generate questions using the provided script:
   ```
   ./generate_enhanced_questions.sh
   ```
4. Build the project

### Automated Build with GitHub Actions

The project includes a GitHub Actions workflow that automatically:
1. Generates all questions
2. Builds the APK
3. Makes the APK available as an artifact

## License

This project is proprietary and confidential.

## Credits

- Vosk ASR: https://github.com/alphacep/vosk-android
- Dutch language model: https://alphacephei.com/vosk/models