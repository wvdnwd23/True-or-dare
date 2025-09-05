package com.wes.truthdare.core.nlp

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class responsible for detecting intent in text
 */
@Singleton
class IntentDetector @Inject constructor(
    private val context: Context
) {
    private val intentPatterns: Map<String, List<String>> by lazy {
        loadIntentPatterns()
    }
    
    /**
     * Load the intent patterns from the assets
     */
    private fun loadIntentPatterns(): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        try {
            val inputStream = context.assets.open("nlp/intents.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            
            while (keys.hasNext()) {
                val intent = keys.next()
                val patternsArray = jsonObject.getJSONArray(intent)
                val patterns = mutableListOf<String>()
                
                for (i in 0 until patternsArray.length()) {
                    patterns.add(patternsArray.getString(i).lowercase())
                }
                
                result[intent] = patterns
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return result
    }
    
    /**
     * Detect intent in text
     * @param text The text to analyze
     * @return The detected intent, or null if no intent is detected
     */
    suspend fun detectIntent(text: String): String? = withContext(Dispatchers.Default) {
        val normalizedText = text.lowercase()
        
        for ((intent, patterns) in intentPatterns) {
            for (pattern in patterns) {
                if (normalizedText.contains(pattern)) {
                    return@withContext intent
                }
            }
        }
        
        return@withContext null
    }
}