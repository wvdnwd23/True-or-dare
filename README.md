# Truth or Dare Deluxe

A completely offline Truth or Dare app built with Kotlin and Jetpack Compose.

## Features

- Completely offline operation - no internet permissions required
- Voice recognition using Vosk for Dutch language
- Follow-up questions based on answers
- Adaptive learning to user preferences
- Privacy-focused design
- 12,000 questions across 12 categories

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

## Building the Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build the project

## License

This project is proprietary and confidential.

## Credits

- Vosk ASR: https://github.com/alphacep/vosk-android
- Dutch language model: https://alphacephei.com/vosk/models