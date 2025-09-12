# Mini-Games Implementation

This directory contains documentation for the mini-games feature in the Truth or Dare Deluxe app.

## Overview

The Truth or Dare Deluxe app includes a Drink Mode feature with 100 mini-games across three difficulty levels:

- **Easy Difficulty**: 33 games
- **Medium Difficulty**: 33 games
- **Hard Difficulty**: 34 games

## Files

- **[mini_games_implementation_summary.md](mini_games_implementation_summary.md)**: Summary of the mini-games implementation
- **[mini_games_details.md](mini_games_details.md)**: Detailed descriptions and instructions for all 100 mini-games
- **[new_mini_games_list.md](new_mini_games_list.md)**: List of the 80 new mini-games added to the existing 20
- **[mini_games_implementation_guide.md](mini_games_implementation_guide.md)**: Guide for implementing the mini-games feature

## Implementation

The mini-games feature is implemented in the following files:

- `app/src/main/java/com/wes/truthdare/ui/screens/drink/DrinkModeViewModel.kt`: Contains the ViewModel for the Drink Mode feature, including the mini-games implementation
- `app/src/main/java/com/wes/truthdare/ui/screens/drink/DrinkModeScreen.kt`: Contains the UI for the Drink Mode feature
- `app/src/main/java/com/wes/truthdare/ui/components/GameCard.kt`: Component for displaying mini-games
- `app/src/main/java/com/wes/truthdare/ui/components/GameInstructionsDialog.kt`: Dialog for displaying mini-game instructions
- `app/src/main/java/com/wes/truthdare/ui/components/DifficultyBadge.kt`: Component for displaying difficulty badges
- `app/src/main/java/com/wes/truthdare/ui/components/DifficultyFilterDialog.kt`: Dialog for filtering mini-games by difficulty

## Features

The mini-games feature includes:

- 100 diverse mini-games across three difficulty levels
- Detailed descriptions and instructions for each game
- Filtering by difficulty level
- Random mini-game selection
- Game instructions with additional tips

## Responsible Drinking Features

The Drink Mode also includes responsible drinking features:

- Water reminder notifications
- Designated driver functionality
- Session time and drink limits
- Emergency contacts access