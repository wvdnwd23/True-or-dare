package com.wes.truthdare.core.data

/**
 * Enum representing different game modes in the app
 */
enum class GameMode {
    CLASSIC,
    DRINK,
    PARTY,
    COUPLES,
    FAMILY,
    TEEN,
    HARDCORE;
    
    /**
     * Get the content filter level for this game mode
     * Used to filter questions based on appropriateness
     */
    fun getContentFilterLevel(): ContentFilterLevel {
        return when (this) {
            CLASSIC -> ContentFilterLevel.MEDIUM
            DRINK -> ContentFilterLevel.HIGH
            PARTY -> ContentFilterLevel.MEDIUM
            COUPLES -> ContentFilterLevel.HIGH
            FAMILY -> ContentFilterLevel.LOW
            TEEN -> ContentFilterLevel.MEDIUM
            HARDCORE -> ContentFilterLevel.EXTREME
        }
    }
    
    /**
     * Check if this game mode is age-restricted
     */
    fun isAgeRestricted(): Boolean {
        return when (this) {
            CLASSIC, PARTY, FAMILY, TEEN -> false
            DRINK, COUPLES, HARDCORE -> true
        }
    }
    
    /**
     * Get the minimum recommended age for this game mode
     */
    fun getMinimumAge(): Int {
        return when (this) {
            FAMILY -> 8
            TEEN -> 13
            CLASSIC, PARTY -> 16
            DRINK, COUPLES, HARDCORE -> 18
        }
    }
    
    /**
     * Get a description of this game mode
     */
    fun getDescription(): String {
        return when (this) {
            CLASSIC -> "The traditional Truth or Dare game with a mix of fun and challenging questions."
            DRINK -> "Party-oriented version with drinking rules, shot counter, and mini-games."
            PARTY -> "Fun, crazy, and group-based dares perfect for livening up any gathering."
            COUPLES -> "Romantic and intimate questions and dares for couples to enjoy together."
            FAMILY -> "Safe, kid-friendly content suitable for family game nights."
            TEEN -> "Age-appropriate questions and dares for teenagers."
            HARDCORE -> "Extreme, adults-only dares and questions for the most daring players."
        }
    }
}

/**
 * Content filter levels for questions and dares
 */
enum class ContentFilterLevel {
    LOW,    // Family-friendly, suitable for all ages
    MEDIUM, // Mild content, suitable for teens
    HIGH,   // Adult content, suitable for 18+
    EXTREME // Very explicit content, strictly 18+
}