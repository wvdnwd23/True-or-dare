package com.wes.truthdare.core.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling app localization
 */
@Singleton
class LocalizationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) {
    /**
     * Apply the saved language to the app context
     * @param context The context to update
     * @return The updated context with the correct locale
     */
    fun applyLanguage(context: Context): Context {
        val languageCode = runBlocking { appPreferences.appLanguage.first() }
        val locale = when (languageCode) {
            "nl" -> Locale("nl")
            "de" -> Locale.GERMAN
            "fr" -> Locale.FRENCH
            "es" -> Locale("es")
            else -> Locale.ENGLISH
        }
        
        return updateContextLocale(context, locale)
    }
    
    /**
     * Set the app language
     * @param language The language to set
     */
    suspend fun setLanguage(language: Language) {
        appPreferences.setAppLanguage(language.locale.language)
    }
    
    /**
     * Get the current app language
     * @return The current language
     */
    suspend fun getCurrentLanguage(): Language {
        val languageCode = appPreferences.appLanguage.first()
        return Language.fromLanguageCode(languageCode)
    }
    
    /**
     * Update the context locale
     * @param context The context to update
     * @param locale The locale to apply
     * @return The updated context
     */
    private fun updateContextLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
}