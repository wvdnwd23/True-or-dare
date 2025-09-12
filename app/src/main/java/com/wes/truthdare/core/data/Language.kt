package com.wes.truthdare.core.data

import java.util.Locale

/**
 * Supported languages in the app
 */
enum class Language(val locale: Locale, val displayName: String) {
    ENGLISH(Locale.ENGLISH, "English"),
    DUTCH(Locale("nl"), "Nederlands"),
    GERMAN(Locale.GERMAN, "Deutsch"),
    FRENCH(Locale.FRENCH, "Français"),
    SPANISH(Locale("es"), "Español");
    
    companion object {
        /**
         * Get the Language enum from a locale
         */
        fun fromLocale(locale: Locale): Language {
            return when (locale.language) {
                "nl" -> DUTCH
                "de" -> GERMAN
                "fr" -> FRENCH
                "es" -> SPANISH
                else -> ENGLISH
            }
        }
        
        /**
         * Get the Language enum from a language code
         */
        fun fromLanguageCode(code: String): Language {
            return when (code.lowercase()) {
                "nl" -> DUTCH
                "de" -> GERMAN
                "fr" -> FRENCH
                "es" -> SPANISH
                else -> ENGLISH
            }
        }
        
        /**
         * Get the system default language
         */
        fun getSystemDefault(): Language {
            return fromLocale(Locale.getDefault())
        }
    }
}