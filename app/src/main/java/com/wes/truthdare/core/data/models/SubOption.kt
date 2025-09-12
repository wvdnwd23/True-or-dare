package com.wes.truthdare.core.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a sub-option within a category
 */
@Entity(
    tableName = "sub_options",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class SubOption(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val name: String,
    val description: String,
    val questionCount: Int = 0,
    val order: Int = 0,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Factory methods for creating sub-options for each category
        
        // Soft category sub-options
        fun icebreakers() = SubOption(
            id = "icebreakers",
            categoryId = "soft",
            name = "Icebreakers",
            description = "Easy questions to get the conversation flowing",
            order = 1
        )
        
        fun funnyMiniDares() = SubOption(
            id = "funny_mini_dares",
            categoryId = "soft",
            name = "Funny Mini-Dares",
            description = "Quick, harmless dares that will make everyone laugh",
            order = 2
        )
        
        fun wouldYouRather() = SubOption(
            id = "would_you_rather",
            categoryId = "soft",
            name = "Would You Rather",
            description = "Choose between two fun or silly options",
            order = 3
        )
        
        // Medium category sub-options
        fun confessions() = SubOption(
            id = "confessions",
            categoryId = "medium",
            name = "Confessions",
            description = "Admit to something you've done or thought",
            order = 1
        )
        
        fun skillShow() = SubOption(
            id = "skill_show",
            categoryId = "medium",
            name = "Skill Show",
            description = "Imitations, singing, acting, and other talents",
            order = 2
        )
        
        fun socialFun() = SubOption(
            id = "social_fun",
            categoryId = "medium",
            name = "Social Fun",
            description = "Light social media prompts and challenges",
            order = 3
        )
        
        // Hot 18+ category sub-options
        fun flirtyTruths() = SubOption(
            id = "flirty_truths",
            categoryId = "hot",
            name = "Flirty Truths",
            description = "Revealing questions about attractions and desires",
            order = 1
        )
        
        fun kissingTouchDares() = SubOption(
            id = "kissing_touch_dares",
            categoryId = "hot",
            name = "Kissing / Touch Dares",
            description = "Physical challenges involving kissing or touching",
            order = 2
        )
        
        fun preferenceScale() = SubOption(
            id = "preference_scale",
            categoryId = "hot",
            name = "Preference Scale",
            description = "Rate your likes and dislikes on various topics",
            order = 3
        )
        
        // Extreme 18+ category sub-options
        fun roleplayChallenges() = SubOption(
            id = "roleplay_challenges",
            categoryId = "extreme",
            name = "Roleplay Challenges",
            description = "Act out different scenarios and characters",
            order = 1
        )
        
        fun fantasyExposure() = SubOption(
            id = "fantasy_exposure",
            categoryId = "extreme",
            name = "Fantasy Exposure",
            description = "Share your deepest fantasies and desires",
            order = 2
        )
        
        fun riskyDares() = SubOption(
            id = "risky_dares",
            categoryId = "extreme",
            name = "Risky Dares",
            description = "Bold and daring challenges for the brave",
            order = 3
        )
        
        // Couples category sub-options
        fun relationshipMemories() = SubOption(
            id = "relationship_memories",
            categoryId = "couples",
            name = "Relationship Memories",
            description = "Reflect on special moments you've shared together",
            order = 1
        )
        
        fun loveLanguages() = SubOption(
            id = "love_languages",
            categoryId = "couples",
            name = "Love Languages",
            description = "Explore how you express and receive love",
            order = 2
        )
        
        fun futureGoalsTogether() = SubOption(
            id = "future_goals_together",
            categoryId = "couples",
            name = "Future Goals Together",
            description = "Discuss your hopes and plans for the future",
            order = 3
        )
        
        // Couples Hot category sub-options
        fun bedroomPreferences() = SubOption(
            id = "bedroom_preferences",
            categoryId = "couples_hot",
            name = "Bedroom Preferences",
            description = "Discuss intimate preferences and desires",
            order = 1
        )
        
        fun fantasyExchange() = SubOption(
            id = "fantasy_exchange",
            categoryId = "couples_hot",
            name = "Fantasy Exchange",
            description = "Share fantasies and scenarios with your partner",
            order = 2
        )
        
        fun intimacyDares() = SubOption(
            id = "intimacy_dares",
            categoryId = "couples_hot",
            name = "Intimacy Dares",
            description = "Physical challenges to enhance your connection",
            order = 3
        )
        
        // Dirty category sub-options
        fun nsfwTruths() = SubOption(
            id = "nsfw_truths",
            categoryId = "dirty",
            name = "NSFW Truths",
            description = "Explicit questions about your experiences",
            order = 1
        )
        
        fun spicyDares() = SubOption(
            id = "spicy_dares",
            categoryId = "dirty",
            name = "Spicy Dares",
            description = "Bold and provocative challenges",
            order = 2
        )
        
        fun truthOrDrink() = SubOption(
            id = "truth_or_drink",
            categoryId = "dirty",
            name = "Truth-or-Drink",
            description = "Answer honestly or take a drink",
            order = 3
        )
        
        // Deep Past category sub-options
        fun firstTimes() = SubOption(
            id = "first_times",
            categoryId = "deep_past",
            name = "First Times",
            description = "Reflect on significant firsts in your life",
            order = 1
        )
        
        fun turningPoints() = SubOption(
            id = "turning_points",
            categoryId = "deep_past",
            name = "Turning Points",
            description = "Moments that changed your life's direction",
            order = 2
        )
        
        fun regretsLessons() = SubOption(
            id = "regrets_lessons",
            categoryId = "deep_past",
            name = "Regrets & Lessons",
            description = "What you've learned from past mistakes",
            order = 3
        )
        
        // Mental Health category sub-options
        fun copingStress() = SubOption(
            id = "coping_stress",
            categoryId = "mental_health",
            name = "Coping & Stress",
            description = "How you handle life's challenges",
            order = 1
        )
        
        fun boundaries() = SubOption(
            id = "boundaries",
            categoryId = "mental_health",
            name = "Boundaries",
            description = "Exploring personal limits and needs",
            order = 2
        )
        
        fun honestCheckIn() = SubOption(
            id = "honest_check_in",
            categoryId = "mental_health",
            name = "Honest Check-In",
            description = "Share how you're really feeling right now",
            order = 3
        )
        
        // Childhood category sub-options
        fun schoolStories() = SubOption(
            id = "school_stories",
            categoryId = "childhood",
            name = "School Stories",
            description = "Memories from your school days",
            order = 1
        )
        
        fun familyTraditions() = SubOption(
            id = "family_traditions",
            categoryId = "childhood",
            name = "Family Traditions",
            description = "Special customs and routines from growing up",
            order = 2
        )
        
        fun embarrassingKidMoments() = SubOption(
            id = "embarrassing_kid_moments",
            categoryId = "childhood",
            name = "Embarrassing Kid Moments",
            description = "Funny or awkward childhood memories",
            order = 3
        )
        
        // Self-Growth category sub-options
        fun habitsGoals() = SubOption(
            id = "habits_goals",
            categoryId = "self_growth",
            name = "Habits & Goals",
            description = "Daily practices and aspirations",
            order = 1
        )
        
        fun dreamBig() = SubOption(
            id = "dream_big",
            categoryId = "self_growth",
            name = "Dream Big",
            description = "Your biggest hopes and ambitions",
            order = 2
        )
        
        fun thirtyDayChallenges() = SubOption(
            id = "thirty_day_challenges",
            categoryId = "self_growth",
            name = "30-Day Challenges",
            description = "Commit to a month of positive change",
            order = 3
        )
        
        // Trauma & Healing category sub-options
        fun safeSharing() = SubOption(
            id = "safe_sharing",
            categoryId = "trauma_healing",
            name = "Safe Sharing",
            description = "Discuss difficult experiences in a supportive space",
            order = 1
        )
        
        fun triggersComfort() = SubOption(
            id = "triggers_comfort",
            categoryId = "trauma_healing",
            name = "Triggers & Comfort",
            description = "Identify what upsets you and what helps",
            order = 2
        )
        
        fun consentBoundaries() = SubOption(
            id = "consent_boundaries",
            categoryId = "trauma_healing",
            name = "Consent & Boundaries",
            description = "Explore personal boundaries and respect",
            order = 3
        )
        
        // Personal category sub-options
        fun innerTruths() = SubOption(
            id = "inner_truths",
            categoryId = "personal",
            name = "Inner Truths",
            description = "Fears, secrets, and honest reflections",
            order = 1
        )
        
        fun vulnerabilities() = SubOption(
            id = "vulnerabilities",
            categoryId = "personal",
            name = "Vulnerabilities",
            description = "Insecurities and sensitive aspects of yourself",
            order = 2
        )
        
        fun lifeChangingMoments() = SubOption(
            id = "life_changing_moments",
            categoryId = "personal",
            name = "Life-Changing Moments",
            description = "Events that fundamentally changed you",
            order = 3
        )
        
        // Self-Discovery category sub-options
        fun valuesIdentity() = SubOption(
            id = "values_identity",
            categoryId = "self_discovery",
            name = "Values & Identity",
            description = "What matters most to you and shapes who you are",
            order = 1
        )
        
        fun blindSpots() = SubOption(
            id = "blind_spots",
            categoryId = "self_discovery",
            name = "Blind Spots",
            description = "Things about yourself you might not see clearly",
            order = 2
        )
        
        fun futureSelf() = SubOption(
            id = "future_self",
            categoryId = "self_discovery",
            name = "Future Self",
            description = "Who you hope to become in the next 10 years",
            order = 3
        )
        
        // Get all predefined sub-options
        fun getAllPredefinedSubOptions(): List<SubOption> {
            return listOf(
                // Soft category
                icebreakers(),
                funnyMiniDares(),
                wouldYouRather(),
                
                // Medium category
                confessions(),
                skillShow(),
                socialFun(),
                
                // Hot 18+ category
                flirtyTruths(),
                kissingTouchDares(),
                preferenceScale(),
                
                // Extreme 18+ category
                roleplayChallenges(),
                fantasyExposure(),
                riskyDares(),
                
                // Couples category
                relationshipMemories(),
                loveLanguages(),
                futureGoalsTogether(),
                
                // Couples Hot category
                bedroomPreferences(),
                fantasyExchange(),
                intimacyDares(),
                
                // Dirty category
                nsfwTruths(),
                spicyDares(),
                truthOrDrink(),
                
                // Deep Past category
                firstTimes(),
                turningPoints(),
                regretsLessons(),
                
                // Mental Health category
                copingStress(),
                boundaries(),
                honestCheckIn(),
                
                // Childhood category
                schoolStories(),
                familyTraditions(),
                embarrassingKidMoments(),
                
                // Self-Growth category
                habitsGoals(),
                dreamBig(),
                thirtyDayChallenges(),
                
                // Trauma & Healing category
                safeSharing(),
                triggersComfort(),
                consentBoundaries(),
                
                // Personal category
                innerTruths(),
                vulnerabilities(),
                lifeChangingMoments(),
                
                // Self-Discovery category
                valuesIdentity(),
                blindSpots(),
                futureSelf()
            )
        }
    }
}