package com.wes.truthdare.ui.screens.categories

import com.wes.truthdare.core.data.IntensityLevel
import com.wes.truthdare.core.data.models.Category
import com.wes.truthdare.core.data.models.SubOption

/**
 * Data class representing the state of category selection
 */
data class CategorySelectionState(
    val categories: List<Category> = emptyList(),
    val subOptions: List<SubOption> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val selectedSubOptionIds: Map<String, Set<String>> = emptyMap(),
    val intensitySliderValue: Int = 50,
    val maxIntensityLevel: IntensityLevel = IntensityLevel.MEDIUM,
    val showAgeRestricted: Boolean = false,
    val showOptInRequired: Boolean = false,
    val ageVerified: Boolean = false,
    val traumaHealingOptedIn: Boolean = false,
    val previewQuestions: List<String> = emptyList()
) {
    /**
     * Get all selected categories
     * @return List of selected Category objects
     */
    fun getSelectedCategories(): List<Category> {
        return categories.filter { it.id in selectedCategoryIds }
    }
    
    /**
     * Get all sub-options for a specific category
     * @param categoryId The category ID
     * @return List of SubOption objects for the category
     */
    fun getSubOptionsForCategory(categoryId: String): List<SubOption> {
        return subOptions.filter { it.categoryId == categoryId }
    }
    
    /**
     * Get selected sub-options for a specific category
     * @param categoryId The category ID
     * @return List of selected SubOption objects for the category
     */
    fun getSelectedSubOptionsForCategory(categoryId: String): List<SubOption> {
        val selectedIds = selectedSubOptionIds[categoryId] ?: emptySet()
        return subOptions.filter { it.categoryId == categoryId && it.id in selectedIds }
    }
    
    /**
     * Check if a category has any selected sub-options
     * @param categoryId The category ID
     * @return True if the category has any selected sub-options
     */
    fun hasSelectedSubOptions(categoryId: String): Boolean {
        return selectedSubOptionIds[categoryId]?.isNotEmpty() == true
    }
    
    /**
     * Get all selected sub-option IDs across all categories
     * @return Set of all selected sub-option IDs
     */
    fun getAllSelectedSubOptionIds(): Set<String> {
        return selectedSubOptionIds.values.flatten().toSet()
    }
    
    /**
     * Check if a category requires age verification
     * @param categoryId The category ID
     * @return True if the category requires age verification
     */
    fun categoryRequiresAgeVerification(categoryId: String): Boolean {
        return categories.find { it.id == categoryId }?.isAgeRestricted == true
    }
    
    /**
     * Check if a category requires opt-in
     * @param categoryId The category ID
     * @return True if the category requires opt-in
     */
    fun categoryRequiresOptIn(categoryId: String): Boolean {
        return categories.find { it.id == categoryId }?.requiresOptIn == true
    }
    
    /**
     * Check if a category can be selected based on age verification and opt-in status
     * @param categoryId The category ID
     * @return True if the category can be selected
     */
    fun canSelectCategory(categoryId: String): Boolean {
        val category = categories.find { it.id == categoryId } ?: return false
        
        if (category.isAgeRestricted && !ageVerified) {
            return false
        }
        
        if (category.requiresOptIn && !traumaHealingOptedIn && category.id == "trauma_healing") {
            return false
        }
        
        return true
    }
    
    /**
     * Get the total number of selected categories
     * @return Number of selected categories
     */
    fun getSelectedCategoryCount(): Int {
        return selectedCategoryIds.size
    }
    
    /**
     * Get the total number of selected sub-options across all categories
     * @return Number of selected sub-options
     */
    fun getSelectedSubOptionCount(): Int {
        return selectedSubOptionIds.values.sumOf { it.size }
    }
    
    /**
     * Create a summary string of the selection
     * @return A string summarizing the selection
     */
    fun getSelectionSummary(): String {
        val categoryCount = getSelectedCategoryCount()
        val subOptionCount = getSelectedSubOptionCount()
        
        return "Selected: $categoryCount categories • $subOptionCount sub-options"
    }
}

/**
 * Data class representing a quick pick preset
 */
data class QuickPickPreset(
    val name: String,
    val description: String,
    val categoryIds: Set<String>,
    val subOptionIds: Map<String, Set<String>>,
    val intensityValue: Int = 50
) {
    companion object {
        /**
         * Party Quick Pick preset
         */
        fun partyQuickPick() = QuickPickPreset(
            name = "Party Quick Pick",
            description = "Fun and lively content for parties",
            categoryIds = setOf("soft", "medium", "dirty"),
            subOptionIds = mapOf(
                "soft" to setOf("funny_mini_dares", "would_you_rather"),
                "medium" to setOf("confessions", "skill_show", "social_fun"),
                "dirty" to setOf("nsfw_truths", "spicy_dares", "truth_or_drink")
            ),
            intensityValue = 75
        )
        
        /**
         * Deep Quick Pick preset
         */
        fun deepQuickPick() = QuickPickPreset(
            name = "Deep Quick Pick",
            description = "Meaningful conversations and self-reflection",
            categoryIds = setOf("personal", "self_discovery", "deep_past"),
            subOptionIds = mapOf(
                "personal" to setOf("inner_truths", "vulnerabilities", "life_changing_moments"),
                "self_discovery" to setOf("values_identity", "blind_spots", "future_self"),
                "deep_past" to setOf("first_times", "turning_points", "regrets_lessons")
            ),
            intensityValue = 50
        )
        
        /**
         * Couples Quick Pick preset
         */
        fun couplesQuickPick() = QuickPickPreset(
            name = "Couples Quick Pick",
            description = "Strengthen your relationship and have fun together",
            categoryIds = setOf("couples", "couples_hot", "personal"),
            subOptionIds = mapOf(
                "couples" to setOf("relationship_memories", "love_languages", "future_goals_together"),
                "couples_hot" to setOf("bedroom_preferences", "fantasy_exchange", "intimacy_dares"),
                "personal" to setOf("inner_truths", "vulnerabilities")
            ),
            intensityValue = 65
        )
        
        /**
         * Get all quick pick presets
         * @return List of all quick pick presets
         */
        fun getAllPresets(): List<QuickPickPreset> {
            return listOf(
                partyQuickPick(),
                deepQuickPick(),
                couplesQuickPick()
            )
        }
    }
}