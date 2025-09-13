# Truth or Dare - Question Filtering Logic

## Overview

This document outlines the filtering logic for the Truth or Dare game, which determines which items (truths and dares) are served to players based on their category selections, sub-option choices, intensity settings, and safety requirements.

## Core Filtering Principles

1. **Category-Based Filtering**: Items are only served if their category is selected
2. **Sub-Option Filtering**: Items must match selected sub-options (if any)
3. **Intensity Filtering**: Items must match or be below the selected intensity level
4. **Safety Filtering**: Age restrictions and opt-in requirements must be respected

## Detailed Filtering Logic

### 1. Category Selection Filter

```
function shouldIncludeItem(item, selectedCategories) {
    // Only include items from selected categories
    return selectedCategories.includes(item.category);
}
```

**Rules:**
- If a category is not selected, no items from that category will be served
- At least one category must be selected for the game to start
- Quick Picks automatically select predefined sets of categories

### 2. Sub-Option Filtering

```
function matchesSubOptions(item, selectedSubOptions) {
    // If no sub-options are selected for this category, include all items from the category
    if (selectedSubOptions[item.category].length === 0) {
        return true;
    }
    
    // Check if the item matches any of the selected sub-options for its category
    return item.subOptions.some(subOption => 
        selectedSubOptions[item.category].includes(subOption)
    );
}
```

**Rules:**
- If specific sub-options are selected for a category, items must match at least one of those sub-options
- If no sub-options are selected for a category, all items from that category are included
- Items can belong to multiple sub-options, matching any selected sub-option is sufficient

### 3. Intensity Filtering

```
function matchesIntensity(item, intensityLevel) {
    const intensityValues = {
        "soft": 1,
        "medium": 2,
        "hot": 3
    };
    
    // Item intensity must be less than or equal to selected intensity
    return intensityValues[item.intensity] <= intensityValues[intensityLevel];
}
```

**Rules:**
- Each item has an intensity label: "soft", "medium", or "hot"
- The intensity slider sets the maximum intensity level
- "Soft" setting only shows items labeled as "soft"
- "Medium" setting shows items labeled as "soft" or "medium"
- "Hot" setting shows items labeled as "soft", "medium", or "hot"
- Progressive Mode overrides this setting and manages intensity progression automatically

### 4. Safety Filtering

```
function passesAgeRestriction(item, userAgeVerified) {
    // If item is age restricted, user must be age verified
    return !item.ageRestricted || userAgeVerified;
}

function passesOptInRequirement(item, optedInCategories) {
    // If item requires opt-in, category must be opted into
    return !item.requiresOptIn || optedInCategories.includes(item.category);
}
```

**Rules:**
- Age-restricted items (from 18+ categories) are only shown if age verification is completed
- Items from opt-in categories are only shown if the user has explicitly opted in
- Family/Teen Mode automatically excludes all age-restricted content
- Explicit opt-in categories (Trauma & Healing) require additional confirmation

### 5. Combined Filtering Logic

```
function filterItems(allItems, selectedCategories, selectedSubOptions, intensityLevel, userAgeVerified, optedInCategories) {
    return allItems.filter(item => 
        shouldIncludeItem(item, selectedCategories) &&
        matchesSubOptions(item, selectedSubOptions) &&
        matchesIntensity(item, intensityLevel) &&
        passesAgeRestriction(item, userAgeVerified) &&
        passesOptInRequirement(item, optedInCategories)
    );
}
```

## Special Mode Filtering

### Mix Mode Filtering

In Mix Mode, the standard filtering logic applies first to create the pool of available items. Then:

1. Items are grouped by category and sub-option
2. The randomization algorithm ensures balanced distribution:
   - No more than 2-3 consecutive items from the same category
   - Approximately equal representation of selected categories
   - Truth/Dare balance maintained at ~50/50 (±10%)

```
function getMixModeItem(filteredItems, recentCategories) {
    // Group items by category
    const itemsByCategory = groupByCategory(filteredItems);
    
    // Avoid recently used categories
    const availableCategories = Object.keys(itemsByCategory)
        .filter(category => !recentCategories.includes(category));
    
    // If all categories were recently used, reset
    const targetCategories = availableCategories.length > 0 ? 
        availableCategories : Object.keys(itemsByCategory);
    
    // Select random category with weighting
    const selectedCategory = selectWeightedRandomCategory(targetCategories);
    
    // Select random item from category
    return selectRandomItem(itemsByCategory[selectedCategory]);
}
```

### Progressive Mode Filtering

In Progressive Mode, intensity filtering changes throughout the session:

1. Game begins with only "soft" intensity items
2. After a set number of rounds (default: 10), "medium" intensity items are included
3. After another set number of rounds (default: 15), "hot" intensity items are included (if age-appropriate)

```
function getProgressiveModeIntensity(roundNumber) {
    if (roundNumber < 10) {
        return "soft";
    } else if (roundNumber < 25) {
        return "medium";
    } else {
        return "hot";
    }
}

function filterProgressiveModeItems(allItems, selectedCategories, selectedSubOptions, roundNumber, userAgeVerified, optedInCategories) {
    const currentIntensity = getProgressiveModeIntensity(roundNumber);
    
    return filterItems(
        allItems, 
        selectedCategories, 
        selectedSubOptions, 
        currentIntensity,
        userAgeVerified, 
        optedInCategories
    );
}
```

## Edge Cases and Special Handling

### Empty Result Handling

If filtering results in no available items:

1. Show a message: "No items match your current selections"
2. Suggest solutions:
   - "Try selecting more categories"
   - "Adjust intensity settings"
   - "Select different sub-options"

### Low Item Count Handling

If filtering results in very few items (less than 20):

1. Show a warning: "Limited items available with current selections"
2. Offer to expand selection automatically
3. Track and avoid repetition of items

### Category Balance Enforcement

To ensure balanced representation:

1. Track category distribution during gameplay
2. If a category is underrepresented (less than 15% of expected):
   - Temporarily increase its selection weight
   - Ensure at least one item from that category appears in the next 5 rounds

### Truth/Dare Balance

To maintain the ~50/50 balance (±10%):

1. Track truth vs. dare count during gameplay
2. If balance exceeds ±15% difference:
   - Temporarily prioritize the underrepresented type
   - Return to random selection once balance is restored

## Implementation Checklist

- [ ] Basic category filtering implementation
- [ ] Sub-option filtering logic
- [ ] Intensity level filtering
- [ ] Age restriction and opt-in enforcement
- [ ] Mix Mode randomization algorithm
- [ ] Progressive Mode intensity progression
- [ ] Empty/low result handling
- [ ] Category balance monitoring
- [ ] Truth/Dare balance monitoring
- [ ] Performance optimization for large item sets