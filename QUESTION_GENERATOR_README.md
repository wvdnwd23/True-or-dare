# Enhanced Question Generator for Truth or Dare Deluxe

This tool generates enhanced question sets for the Truth or Dare Deluxe app, with 1000 questions per mode (100 for drink mode).

## Features

- Generates questions for four game modes:
  - Classic: 1000 questions per category
  - Couples: 1000 questions per category
  - Party: 1000 questions per category
  - Drink: 100 questions per category
- 12 categories per mode:
  - casual
  - party
  - deep
  - romantic
  - family
  - friends
  - funny
  - challenge
  - personal
  - childhood
  - future
  - hypothetical
- Mode-specific questions and templates
- Enhanced tagging system for better question selection
- Drink mode with responsible drinking challenges

## How to Generate Questions

### Option 1: Using the Script

1. Make sure you have Kotlin installed on your system
2. Run the generation script:
   ```
   ./generate_enhanced_questions.sh
   ```
3. The questions will be generated in `app/src/main/assets/questions/modes/`

### Option 2: Using GitHub Actions

1. Push your changes to GitHub
2. The GitHub Actions workflow will automatically:
   - Generate the questions
   - Build the APK
   - Make the APK available as an artifact

## Question Structure

Each question is structured as follows:

```json
{
  "id": "unique-id",
  "type": "truth|dare",
  "category": "category-name",
  "targets": "single|group|pair|self",
  "depthLevel": 1-5,
  "tags": ["tag1", "tag2", "..."],
  "text": "Question text",
  "mode": "classic|couples|party|drink"
}
```

## Customizing Questions

To customize the questions:

1. Edit the `enhanced_question_generator.kt` file
2. Modify the templates, synonyms, or tag lists
3. Run the generation script again

## Drink Mode Considerations

The drink mode is limited to 100 questions per category to encourage responsible drinking. All drink challenges include non-alcoholic alternatives and responsible drinking reminders.