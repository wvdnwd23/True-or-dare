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
 * Class responsible for tagging text with semantic categories
 */
@Singleton
class Tagger @Inject constructor(
    private val context: Context
) {
    private val tagMap: Map<String, List<String>> by lazy {
        loadTagMap()
    }
    
    /**
     * Load the tag map from the assets
     */
    private fun loadTagMap(): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        try {
            val inputStream = context.assets.open("nlp/tags.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            
            while (keys.hasNext()) {
                val tag = keys.next()
                val wordsArray = jsonObject.getJSONArray(tag)
                val words = mutableListOf<String>()
                
                for (i in 0 until wordsArray.length()) {
                    words.add(wordsArray.getString(i).lowercase())
                }
                
                result[tag] = words
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return result
    }
    
    /**
     * Extract tags from text
     * @param text The text to analyze
     * @return A list of tags found in the text
     */
    suspend fun extractTags(text: String): List<String> = withContext(Dispatchers.Default) {
        val normalizedText = text.lowercase()
        val words = normalizedText.split(Regex("\\s+|[,.!?;:]"))
        
        val foundTags = mutableSetOf<String>()
        
        for ((tag, tagWords) in tagMap) {
            if (tagWords.any { word -> words.contains(word) }) {
                foundTags.add(tag)
            }
        }
        
        // Check for multi-word matches
        for ((tag, tagWords) in tagMap) {
            for (tagWord in tagWords) {
                if (tagWord.contains(" ") && normalizedText.contains(tagWord)) {
                    foundTags.add(tag)
                    break
                }
            }
        }
        
        return@withContext foundTags.toList()
    }
}