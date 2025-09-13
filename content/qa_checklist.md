# Truth or Dare - QA Checklist

## Content Quality Verification

### Category Content Requirements

- [ ] Each category has ≥500 items total
- [ ] Each sub-option has ≥120 items
- [ ] Truth vs. Dare balance is approximately 50/50 (±10%) for each category
- [ ] Content is in clear, fun, non-repetitive English
- [ ] No duplicates across all content
- [ ] Party-friendly tone for light categories
- [ ] Respectful, empathetic tone for deep categories

### Category-Specific Checks

#### Soft Category
- [ ] Content is appropriate for all ages
- [ ] Questions and dares are light-hearted and fun
- [ ] All items are labeled with "soft" intensity
- [ ] Sub-options (Icebreaker, Silly, Creative) have distinct content

#### Medium Category
- [ ] Content is moderately challenging but not explicit
- [ ] Questions and dares push boundaries slightly
- [ ] All items are labeled with "medium" intensity
- [ ] Sub-options (Challenging, Entertaining, Surprising) have distinct content

#### Hot (18+) Category
- [ ] All content is appropriately marked as age-restricted
- [ ] Content is spicy but not explicit
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Flirty, Sensual, Daring) have distinct content

#### Extreme (18+) Category
- [ ] All content is appropriately marked as age-restricted
- [ ] Content is intense but within policy guidelines
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Wild, Risqué, Uninhibited) have distinct content

#### Couples Category
- [ ] Content focuses on relationship dynamics
- [ ] Questions and dares are appropriate for couples
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Bonding, Romantic, Playful) have distinct content

#### Couples Hot (18+) Category
- [ ] All content is appropriately marked as age-restricted
- [ ] Content is intimate but within policy guidelines
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Intimate, Passionate, Adventurous) have distinct content

#### Dirty (18+) Category
- [ ] All content is appropriately marked as age-restricted
- [ ] Content is suggestive but within policy guidelines
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Suggestive, Explicit, Fantasy) have distinct content

#### Deep Past Category
- [ ] Content focuses on history and memories
- [ ] Questions are thoughtful and reflective
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Memories, Reflections, Lessons) have distinct content

#### Mental Health Category
- [ ] Content is marked as requiring opt-in
- [ ] Questions and dares are supportive and non-triggering
- [ ] Tone is gentle and empathetic
- [ ] Sub-options (Awareness, Coping, Growth) have distinct content

#### Childhood Category
- [ ] Content focuses on early years and formative experiences
- [ ] Questions are nostalgic and reflective
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Nostalgia, Experiences, Influences) have distinct content

#### Self-Growth Category
- [ ] Content focuses on personal development
- [ ] Questions are constructive and forward-looking
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Goals, Challenges, Insights) have distinct content

#### Trauma & Healing Category
- [ ] Content is marked as requiring explicit opt-in
- [ ] Questions are supportive and non-retraumatizing
- [ ] Tone is gentle, empathetic, and recovery-focused
- [ ] Sub-options (Processing, Recovery, Resilience) have distinct content

#### Personal Category
- [ ] Content focuses on individual traits and preferences
- [ ] Questions are reflective and self-focused
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Values, Preferences, Quirks) have distinct content

#### Self-Discovery Category
- [ ] Content focuses on identity and self-understanding
- [ ] Questions are thoughtful and introspective
- [ ] Items are labeled with appropriate intensity levels
- [ ] Sub-options (Identity, Purpose, Authenticity) have distinct content

## Safety & Policy Compliance

### Age Restriction Verification
- [ ] Hot (18+) category properly marked as age-restricted
- [ ] Extreme (18+) category properly marked as age-restricted
- [ ] Couples Hot (18+) category properly marked as age-restricted
- [ ] Dirty (18+) category properly marked as age-restricted
- [ ] Age verification modal appears when selecting 18+ categories
- [ ] 18+ content is never shown without age verification

### Opt-In Verification
- [ ] Mental Health category requires standard opt-in
- [ ] Trauma & Healing category requires explicit opt-in
- [ ] Opt-in modals contain appropriate warnings and descriptions
- [ ] Opt-in status is required for each new game session
- [ ] Skip options are available for all opt-in category questions

### Content Policy Compliance
- [ ] No content encouraging hate or discrimination
- [ ] No content encouraging violence or illegal activities
- [ ] No content encouraging self-harm
- [ ] No medical advice or diagnosis-related questions
- [ ] Alcohol-related content only in 18+ categories
- [ ] "Know your limits" reminders included with alcohol content
- [ ] No excessive drinking encouragement

## UX Implementation

### Category Selection Screen
- [ ] All 14 categories displayed with icons/emojis
- [ ] Category titles clearly visible
- [ ] 18+ and opt-in badges properly displayed
- [ ] Multi-select functionality works correctly
- [ ] Selected categories are visually distinct

### Sub-Option Selection
- [ ] 3 sub-options appear for each selected category
- [ ] Sub-options are displayed as multi-select chips
- [ ] Selected sub-options are visually distinct
- [ ] Sub-option descriptions available on tap/hover

### Selection Summary
- [ ] "Selected: X categories • Y sub-options" updates correctly
- [ ] Clear All action works properly
- [ ] Summary updates in real-time as selections change

### Quick Picks
- [ ] Party Quick Pick selects appropriate categories
- [ ] Deep Quick Pick selects appropriate categories
- [ ] Couples Quick Pick selects appropriate categories
- [ ] Quick Picks update the selection summary correctly

### Intensity Slider
- [ ] Slider has Soft, Medium, and Hot positions
- [ ] Slider position affects content filtering correctly
- [ ] Visual feedback indicates current intensity level

### Preview Panel
- [ ] Shows representative examples from selected categories
- [ ] Updates when selections change
- [ ] Displays both truths and dares
- [ ] Respects intensity settings

## Filtering Logic

### Category Filtering
- [ ] Only items from selected categories are served
- [ ] If no categories are selected, prompt to select categories

### Sub-Option Filtering
- [ ] If sub-options are selected, only matching items are served
- [ ] If no sub-options are selected for a category, all items from that category are included

### Intensity Filtering
- [ ] Soft setting only shows "soft" items
- [ ] Medium setting shows "soft" and "medium" items
- [ ] Hot setting shows all intensity levels
- [ ] Progressive Mode correctly manages intensity progression

### Safety Filtering
- [ ] Age-restricted items only shown if age verification completed
- [ ] Opt-in category items only shown if opted in
- [ ] Family/Teen Mode never shows 18+ or explicit items

## Session Experience Features

### Mix Mode
- [ ] Randomly alternates across selected categories/sub-options
- [ ] Maintains balanced distribution
- [ ] Avoids consecutive items from same category
- [ ] Maintains Truth/Dare balance

### Progressive Mode
- [ ] Starts with soft intensity items
- [ ] Gradually increases to medium intensity
- [ ] Eventually includes hot intensity (if appropriate)
- [ ] Progression speed is appropriate

### Spotlight Moments
- [ ] Appear at appropriate intervals
- [ ] Special visual treatment distinguishes them
- [ ] Different types of Spotlight Moments are included
- [ ] Featured in End-of-Game Report

### End-of-Game Report
- [ ] Shows counts per category/sub-option
- [ ] Displays Truth/Dare balance
- [ ] Highlights notable moments
- [ ] Provides engaging statistics

## Example Items Per Category

### Soft Category Examples
- Truth (Icebreaker): "What's your favorite way to spend a lazy Sunday?"
- Dare (Silly): "Do your best chicken impression for 15 seconds."
- Truth (Creative): "If you could invent anything, what would it be?"

### Medium Category Examples
- Truth (Challenging): "What's something you've never told anyone in this room?"
- Dare (Entertaining): "Do an impression of another player until someone correctly guesses who it is."
- Truth (Surprising): "What's something that would surprise people to learn about you?"

### Hot (18+) Category Examples
- Truth (Flirty): "What's your biggest turn-on?"
- Dare (Sensual): "Demonstrate your most seductive walk across the room."
- Truth (Daring): "What's the most spontaneous romantic encounter you've had?"

### Mental Health Category Examples
- Truth (Awareness): "What's one small thing you do to take care of your mental health?"
- Dare (Coping): "Share a mantra or positive affirmation that helps you through difficult times."
- Truth (Growth): "How have your experiences with mental health challenges helped you grow?"

## Final Verification

- [ ] All categories meet minimum item counts
- [ ] Truth/Dare balance is maintained across all categories
- [ ] No duplicate content across categories
- [ ] All safety gates and filters work correctly
- [ ] UX is intuitive and responsive
- [ ] Session experience features enhance gameplay
- [ ] End-of-Game Report provides meaningful insights