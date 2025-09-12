package com.wes.truthdare.core.data

/**
 * Enum representing different intensity levels for questions and dares
 */
enum class IntensityLevel {
    SOFT,   // Mild, suitable for all players
    MEDIUM, // Moderate intensity, slightly challenging
    HOT;    // High intensity, more challenging or intimate
    
    companion object {
        /**
         * Get intensity level from a slider value (0-100)
         * @param sliderValue The slider value between 0 and 100
         * @return The maximum intensity level based on the slider position
         */
        fun fromSliderValue(sliderValue: Int): IntensityLevel {
            return when {
                sliderValue < 34 -> SOFT
                sliderValue < 67 -> MEDIUM
                else -> HOT
            }
        }
        
        /**
         * Get the maximum intensity level allowed based on a slider value
         * @param sliderValue The slider value between 0 and 100
         * @return List of allowed intensity levels
         */
        fun getAllowedLevels(sliderValue: Int): List<IntensityLevel> {
            return when {
                sliderValue < 34 -> listOf(SOFT)
                sliderValue < 67 -> listOf(SOFT, MEDIUM)
                else -> listOf(SOFT, MEDIUM, HOT)
            }
        }
        
        /**
         * Get a description of the intensity level
         * @param level The intensity level
         * @return A description of the intensity level
         */
        fun getDescription(level: IntensityLevel): String {
            return when (level) {
                SOFT -> "Mild content suitable for all players"
                MEDIUM -> "Moderately challenging content"
                HOT -> "More intense or intimate content"
            }
        }
    }
}