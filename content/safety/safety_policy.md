# Truth or Dare - Safety & Policy Implementation

## Overview

This document outlines the safety measures and policy implementations for the Truth or Dare game to ensure appropriate content filtering, age restrictions, and opt-in mechanisms for sensitive categories.

## Age Restriction Implementation

### 18+ Categories

The following categories are restricted to users 18 years and older:

1. **Hot (18+)** - Spicy questions and dares for adult players
2. **Extreme (18+)** - The most intense questions and dares for adventurous adult players
3. **Couples Hot (18+)** - Intimate questions and dares for adult couples
4. **Dirty (18+)** - Explicitly suggestive questions and dares for adult players

### Age Verification Process

1. **First-Time Gate**:
   - When a user selects any 18+ category for the first time, an age verification modal appears
   - The modal clearly states: "This category contains adult content intended for users 18 and older"
   - User must confirm they are 18+ to proceed
   - A secondary confirmation asks: "By continuing, you confirm you are at least 18 years old"

2. **Persistent Setting**:
   - Age verification status is stored locally on the device
   - Once verified, the user won't be prompted again on that device
   - A setting in the app allows users to reset all verifications if needed

3. **Visual Indicators**:
   - All 18+ categories are clearly marked with an "18+" badge
   - The badge is prominently displayed on category cards and in selection lists

## Opt-In Mechanisms

### Standard Opt-In Categories

The following category requires a standard opt-in:

1. **Mental Health** - Questions about emotional wellbeing and mental health

### Explicit Opt-In Categories

The following category requires an explicit opt-in with additional warnings:

1. **Trauma & Healing** - Sensitive questions about difficult experiences and recovery

### Opt-In Implementation

1. **Standard Opt-In Process**:
   - When selecting a standard opt-in category, a modal appears with category information
   - The modal explains the nature of the content and potential sensitivity
   - User must click "I understand, continue" to include the category

2. **Explicit Opt-In Process**:
   - For Trauma & Healing, a more detailed warning appears
   - The warning explains that questions may trigger difficult emotions or memories
   - Clear language states: "These questions deal with potentially traumatic experiences"
   - User must explicitly acknowledge understanding before proceeding
   - A secondary confirmation asks if they're in an appropriate setting to discuss sensitive topics

3. **Per-Session Opt-In**:
   - Unlike age verification, opt-ins are required for each new game session
   - This ensures users are making conscious choices each time they play

4. **Skip Options**:
   - All questions from opt-in categories include a prominent "Skip" button
   - No penalty or negative feedback for skipping sensitive questions
   - Skip option is explained during the opt-in process

## Content Filtering Logic

### Category-Based Filtering

1. **Basic Rule**: Items are only served if their category is selected
2. **Sub-Option Logic**:
   - If specific sub-options are selected, only items matching those sub-options are served
   - If no sub-options are selected for a category, all items from that category are included

### Intensity Filtering

1. **Intensity Levels**:
   - Each item has an intensity label: Soft, Medium, or Hot
   - The intensity slider filters content based on these labels

2. **Filtering Logic**:
   - Soft setting: Only shows items labeled as "soft"
   - Medium setting: Shows items labeled as "soft" or "medium"
   - Hot setting: Shows items labeled as "soft", "medium", or "hot"

3. **Override Rules**:
   - Age restrictions always take precedence over intensity settings
   - 18+ content is never shown without age verification, regardless of intensity setting

### Family/Teen Mode

1. **Implementation**:
   - When Family/Teen Mode is enabled, all 18+ categories are automatically excluded
   - Content with explicit language or adult themes is filtered out
   - Intensity is capped at Medium level
   - Opt-in categories require parent/guardian approval

2. **Activation**:
   - Family/Teen Mode can be enabled in settings
   - Optional passcode protection for mode changes

## Alcohol Rules

1. **Restriction to 18+ Sets**:
   - Questions or dares involving alcohol only appear in 18+ categories
   - Always include "Know your limits" reminders

2. **Responsible Messaging**:
   - No content encouraging excessive drinking
   - Alternatives always offered (e.g., "Take a drink or do 10 push-ups")
   - Periodic reminders about hydration and pacing

3. **Exclusion Option**:
   - Setting to exclude all alcohol-related content, even in 18+ categories

## Sensitive Content Handling

For Mental Health, Trauma & Healing, Personal, and Self-Discovery categories:

1. **Tone Guidelines**:
   - Use gentle, empathetic phrasing
   - Avoid judgmental language
   - Frame questions as invitations rather than demands

2. **Skip Functionality**:
   - Prominent skip button on all sensitive questions
   - No penalty or negative feedback for skipping
   - Option to temporarily pause or exit game

3. **Support Resources**:
   - Include access to support resources in settings
   - Brief supportive messages when sensitive categories are selected

## Policy Violations Prevention

1. **Content Review Process**:
   - All questions and dares reviewed against policy guidelines
   - Multiple review passes for sensitive categories

2. **Prohibited Content**:
   - No content encouraging hate, violence, illegal acts, or self-harm
   - No medical advice or diagnosis-related questions
   - No content targeting protected characteristics

3. **User Reporting**:
   - In-game mechanism to report inappropriate content
   - Feedback used to improve content filtering

## Implementation Checklist

- [ ] Age verification modal for 18+ categories
- [ ] Standard opt-in modal for Mental Health category
- [ ] Explicit opt-in modal for Trauma & Healing category
- [ ] Intensity filtering implementation
- [ ] Family/Teen Mode toggle and filtering
- [ ] Skip functionality for sensitive questions
- [ ] Support resources section
- [ ] Content review process documentation
- [ ] User reporting mechanism