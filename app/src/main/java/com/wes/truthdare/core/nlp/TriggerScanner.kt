package com.wes.truthdare.core.nlp

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class responsible for scanning text for trigger words or patterns
 */
@Singleton
class TriggerScanner @Inject constructor(
    private val context: Context
) {
    private data class TriggerData(
        val words: List<String>,
        val patterns: List<Regex>
    )
    
    private val triggerData: TriggerData by lazy {
        loadTriggerData()
    }
    
    /**
     * Load the trigger data from the assets
     */
    private fun loadTriggerData(): TriggerData {
        val words = mutableListOf<String>()
        val patterns = mutableListOf<Regex>()
        
        try {
            val inputStream = context.assets.open("nlp/triggers.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            val jsonObject = JSONObject(jsonString)
            
            // Load trigger words
            val wordsArray = jsonObject.getJSONArray("words")
            for (i in 0 until wordsArray.length()) {
                words.add(wordsArray.getString(i).lowercase())
            }
            
            // Load regex patterns
            val patternsArray = jsonObject.getJSONArray("patterns")
            for (i in 0 until patternsArray.length()) {
                val pattern = patternsArray.getString(i)
                patterns.add(Regex(pattern, RegexOption.IGNORE_CASE))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return TriggerData(words, patterns)
    }
    
    /**
     * Scan text for triggers
     * @param text The text to scan
     * @return True if triggers are found, false otherwise
     */
    suspend fun scanForTriggers(text: String): Boolean = withContext(Dispatchers.Default) {
        val normalizedText = text.lowercase()
        val words = normalizedText.split(Regex("\\s+|[,.!?;:]"))
        
        // Check for trigger words
        if (triggerData.words.any { triggerWord -> words.contains(triggerWord) }) {
            return@withContext true
        }
        
        // Check for multi-word trigger phrases
        if (triggerData.words.any { triggerWord -> 
            triggerWord.contains(" ") && normalizedText.contains(triggerWord) 
        }) {
            return@withContext true
        }
        
        // Check for regex patterns
        if (triggerData.patterns.any { pattern -> pattern.containsMatchIn(normalizedText) }) {
            return@withContext true
        }
        
        return@withContext false
    }
}