package com.wes.truthdare.core.data.repositories

import android.content.Context
import com.wes.truthdare.core.agents.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for question operations
 */
@Singleton
class QuestionRepository @Inject constructor(
    private val context: Context
) {
    // Cache for loaded questions
    private val questionCache = mutableMapOf<String, List<Question>>()
    
    /**
     * Get questions for a category
     * @param category The category to get questions for
     * @return List of questions for the category
     */
    suspend fun getQuestionsForCategory(category: String): List<Question> = withContext(Dispatchers.IO) {
        // Check cache first
        questionCache[category]?.let { return@withContext it }
        
        // Load questions from JSON file
        val questions = loadQuestionsFromAsset(category)
        
        // Cache the questions
        questionCache[category] = questions
        
        return@withContext questions
    }
    
    /**
     * Get questions for multiple categories
     * @param categories The categories to get questions for
     * @return Map of category to list of questions
     */
    suspend fun getQuestionsForCategories(categories: List<String>): Map<String, List<Question>> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, List<Question>>()
        
        for (category in categories) {
            result[category] = getQuestionsForCategory(category)
        }
        
        return@withContext result
    }
    
    /**
     * Get questions by type and categories
     * @param type The question type ("truth" or "dare")
     * @param categories The categories to get questions for
     * @return List of questions matching the criteria
     */
    suspend fun getQuestionsByTypeAndCategories(type: String, categories: List<String>): List<Question> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Question>()
        
        for (category in categories) {
            val questions = getQuestionsForCategory(category)
            result.addAll(questions.filter { it.type == type })
        }
        
        return@withContext result
    }
    
    /**
     * Get questions by tags
     * @param tags The tags to search for
     * @param categories The categories to search in
     * @return List of questions with the specified tags
     */
    suspend fun getQuestionsByTags(tags: List<String>, categories: List<String>): List<Question> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Question>()
        
        for (category in categories) {
            val questions = getQuestionsForCategory(category)
            result.addAll(questions.filter { question ->
                question.tags.any { tag -> tags.contains(tag) }
            })
        }
        
        return@withContext result
    }
    
    /**
     * Get a question by ID
     * @param id The question ID
     * @return The question, or null if not found
     */
    suspend fun getQuestionById(id: String): Question? = withContext(Dispatchers.IO) {
        for ((_, questions) in questionCache) {
            val question = questions.find { it.id == id }
            if (question != null) {
                return@withContext question
            }
        }
        
        // If not found in cache, load all categories and search again
        val categories = listOf(
            "casual", "party", "deep", "romantic", "family", "friends",
            "funny", "challenge", "personal", "childhood", "future", "hypothetical"
        )
        
        for (category in categories) {
            val questions = getQuestionsForCategory(category)
            val question = questions.find { it.id == id }
            if (question != null) {
                return@withContext question
            }
        }
        
        return@withContext null
    }
    
    /**
     * Load questions from an asset file
     * @param category The category to load
     * @return List of questions from the asset file
     */
    private suspend fun loadQuestionsFromAsset(category: String): List<Question> = withContext(Dispatchers.IO) {
        val questions = mutableListOf<Question>()
        
        try {
            val inputStream = context.assets.open("questions/$category.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            val jsonArray = JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                
                val tagsArray = jsonObject.getJSONArray("tags")
                val tags = mutableListOf<String>()
                for (j in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(j))
                }
                
                val question = Question(
                    id = jsonObject.getString("id"),
                    type = jsonObject.getString("type"),
                    category = jsonObject.getString("category"),
                    targets = jsonObject.getString("targets"),
                    depthLevel = if (jsonObject.has("depthLevel")) jsonObject.getInt("depthLevel") else null,
                    tags = tags,
                    text = jsonObject.getString("text")
                )
                
                questions.add(question)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return@withContext questions
    }
    
    /**
     * Clear the question cache
     */
    fun clearCache() {
        questionCache.clear()
    }
}