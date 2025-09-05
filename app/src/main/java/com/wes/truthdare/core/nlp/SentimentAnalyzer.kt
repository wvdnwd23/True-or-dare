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
 * Class responsible for analyzing sentiment in text
 */
@Singleton
class SentimentAnalyzer @Inject constructor(
    private val context: Context
) {
    private val sentimentScores: Map<String, Int> by lazy {
        loadSentimentScores()
    }
    
    /**
     * Load the sentiment scores from the assets
     */
    private fun loadSentimentScores(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        try {
            val inputStream = context.assets.open("nlp/sentiment.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            
            while (keys.hasNext()) {
                val word = keys.next()
                val score = jsonObject.getInt(word)
                result[word.lowercase()] = score
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return result
    }
    
    /**
     * Analyze sentiment in text
     * @param text The text to analyze
     * @return A sentiment score from -100 (very negative) to 100 (very positive)
     */
    suspend fun analyzeSentiment(text: String): Int = withContext(Dispatchers.Default) {
        val normalizedText = text.lowercase()
        val words = normalizedText.split(Regex("\\s+|[,.!?;:]"))
        
        var totalScore = 0
        var wordCount = 0
        
        for (word in words) {
            if (word.isNotBlank()) {
                sentimentScores[word]?.let { score ->
                    totalScore += score
                    wordCount++
                }
            }
        }
        
        // Check for multi-word sentiment phrases
        for ((phrase, score) in sentimentScores) {
            if (phrase.contains(" ") && normalizedText.contains(phrase)) {
                totalScore += score
                wordCount++
            }
        }
        
        // Normalize score to -100 to 100 range
        return@withContext if (wordCount > 0) {
            (totalScore * 100) / (wordCount * 5) // Assuming scores are in -5 to 5 range
        } else {
            0 // Neutral if no sentiment words found
        }
    }
}