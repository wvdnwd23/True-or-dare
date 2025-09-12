package com.wes.truthdare.core.data.repositories

import android.content.Context
import com.wes.truthdare.core.data.AppPreferences
import com.wes.truthdare.core.data.ContentFilterLevel
import com.wes.truthdare.core.data.GameMode
import com.wes.truthdare.core.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced repository for managing questions with support for all game modes
 * and advanced filtering capabilities
 */
@Singleton
class EnhancedQuestionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) {
    // Cache of all questions by category
    private val questionCache = mutableMapOf<String, List<EnhancedQuestion>>()
    
    // Cache of custom user-generated questions
    private val customQuestionsCache = mutableStateOf<List<EnhancedQuestion>>(emptyList())
    
    // Flow of the current content filter level
    private val contentFilterLevel = appPreferences.contentFilterLevel
        .stateIn(MutableStateFlow(ContentFilterLevel.MEDIUM))
    
    init {
        // Load custom questions
        loadCustomQuestions()
    }
    
    /**
     * Get questions for a specific game mode, filtered by content level and other criteria
     */
    suspend fun getQuestionsForGameMode(
        gameMode: GameMode,
        questionType: QuestionType? = null,
        difficulty: QuestionDifficulty? = null,
        category: String? = null,
        includeCustomQuestions: Boolean = true,
        includePremiumQuestions: Boolean = false,
        limit: Int = 100
    ): Flow<List<EnhancedQuestion>> = flow {
        val allQuestions = mutableListOf<EnhancedQuestion>()
        
        // Get the current content filter level
        val currentFilterLevel = contentFilterLevel.value
        
        // Load questions from all categories
        val categories = category?.let { listOf(it) } ?: getCompatibleCategories(gameMode)
        
        for (cat in categories) {
            val categoryQuestions = getQuestionsForCategory(cat)
            allQuestions.addAll(categoryQuestions)
        }
        
        // Add custom questions if requested
        if (includeCustomQuestions) {
            allQuestions.addAll(customQuestionsCache.value)
        }
        
        // Filter questions based on criteria
        val filteredQuestions = allQuestions.filter { question ->
            // Filter by game mode compatibility
            val gameModeMatch = question.compatibleGameModes.contains(gameMode)
            
            // Filter by content level
            val contentLevelMatch = question.contentFilterLevel.ordinal <= currentFilterLevel.ordinal
            
            // Filter by question type if specified
            val typeMatch = questionType?.let { it == question.type } ?: true
            
            // Filter by difficulty if specified
            val difficultyMatch = difficulty?.let { it == question.difficulty } ?: true
            
            // Filter by premium status
            val premiumMatch = if (includePremiumQuestions) true else !question.isPremium
            
            gameModeMatch && contentLevelMatch && typeMatch && difficultyMatch && premiumMatch
        }
        
        // Shuffle and limit the results
        emit(filteredQuestions.shuffled().take(limit))
    }
    
    /**
     * Get questions for a specific category
     */
    private suspend fun getQuestionsForCategory(category: String): List<EnhancedQuestion> {
        return withContext(Dispatchers.IO) {
            // Check if questions for this category are already cached
            questionCache[category]?.let { return@withContext it }
            
            try {
                // Load questions from JSON file
                val inputStream = context.assets.open("questions/$category.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                
                // Parse questions
                val questions = mutableListOf<EnhancedQuestion>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val questionMap = jsonToMap(jsonObject)
                    val enhancedQuestion = questionMap.toEnhancedQuestion()
                    questions.add(enhancedQuestion)
                }
                
                // Cache the questions
                questionCache[category] = questions
                
                questions
            } catch (e: Exception) {
                // If there's an error, return an empty list
                emptyList()
            }
        }
    }
    
    /**
     * Get categories that are compatible with a specific game mode
     */
    private fun getCompatibleCategories(gameMode: GameMode): List<String> {
        return when (gameMode) {
            GameMode.CLASSIC -> listOf("casual", "party", "deep", "funny", "challenge", "personal", "hypothetical")
            GameMode.DRINK -> listOf("party", "challenge", "funny")
            GameMode.PARTY -> listOf("party", "funny", "challenge", "hypothetical")
            GameMode.COUPLES -> listOf("romantic", "deep", "personal", "hypothetical")
            GameMode.FAMILY -> listOf("family", "casual", "funny", "childhood")
            GameMode.TEEN -> listOf("casual", "funny", "friends", "hypothetical", "future")
            GameMode.HARDCORE -> listOf("party", "challenge", "deep", "personal")
        }
    }
    
    /**
     * Convert a JSONObject to a Map
     */
    private fun jsonToMap(jsonObject: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()
        
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            
            when (value) {
                is JSONObject -> map[key] = jsonToMap(value)
                is JSONArray -> {
                    val list = mutableListOf<Any>()
                    for (i in 0 until value.length()) {
                        list.add(value.get(i))
                    }
                    map[key] = list
                }
                else -> map[key] = value
            }
        }
        
        return map
    }
    
    /**
     * Get daily dare question
     */
    fun getDailyDare(): Flow<EnhancedQuestion?> = flow {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        // Use the date as a seed for the random number generator
        // This ensures the same question is selected for the entire day
        val random = Random(today)
        
        // Get all questions
        val allQuestions = mutableListOf<EnhancedQuestion>()
        getCompatibleCategories(GameMode.CLASSIC).forEach { category ->
            allQuestions.addAll(getQuestionsForCategory(category))
        }
        
        // Filter questions by content level
        val currentFilterLevel = contentFilterLevel.value
        val filteredQuestions = allQuestions.filter { 
            it.contentFilterLevel.ordinal <= currentFilterLevel.ordinal 
        }
        
        // Select a random question for today
        val dailyQuestion = if (filteredQuestions.isNotEmpty()) {
            val index = random.nextInt(filteredQuestions.size)
            filteredQuestions[index].copy(isDaily = true)
        } else {
            null
        }
        
        emit(dailyQuestion)
    }
    
    /**
     * Add a custom question
     */
    suspend fun addCustomQuestion(question: EnhancedQuestion) {
        withContext(Dispatchers.IO) {
            val customQuestions = customQuestionsCache.value.toMutableList()
            customQuestions.add(question)
            customQuestionsCache.value = customQuestions
            saveCustomQuestions()
        }
    }
    
    /**
     * Delete a custom question
     */
    suspend fun deleteCustomQuestion(questionId: String) {
        withContext(Dispatchers.IO) {
            val customQuestions = customQuestionsCache.value.toMutableList()
            customQuestions.removeAll { it.id == questionId }
            customQuestionsCache.value = customQuestions
            saveCustomQuestions()
        }
    }
    
    /**
     * Get all custom questions
     */
    fun getCustomQuestions(): Flow<List<EnhancedQuestion>> = flow {
        emit(customQuestionsCache.value)
    }
    
    /**
     * Load custom questions from storage
     */
    private fun loadCustomQuestions() {
        try {
            val file = File(context.filesDir, "custom_questions.json")
            if (file.exists()) {
                val jsonString = file.readText()
                val jsonArray = JSONArray(jsonString)
                
                val questions = mutableListOf<EnhancedQuestion>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val questionMap = jsonToMap(jsonObject)
                    val enhancedQuestion = questionMap.toEnhancedQuestion().copy(isUserGenerated = true)
                    questions.add(enhancedQuestion)
                }
                
                customQuestionsCache.value = questions
            }
        } catch (e: Exception) {
            // If there's an error, keep the empty list
        }
    }
    
    /**
     * Save custom questions to storage
     */
    private fun saveCustomQuestions() {
        try {
            val file = File(context.filesDir, "custom_questions.json")
            val jsonArray = JSONArray()
            
            for (question in customQuestionsCache.value) {
                val jsonObject = JSONObject()
                jsonObject.put("id", question.id)
                jsonObject.put("type", question.type.name)
                jsonObject.put("text", question.text)
                jsonObject.put("category", question.category)
                jsonObject.put("targets", question.targets.name)
                jsonObject.put("difficulty", question.difficulty.name)
                jsonObject.put("contentFilterLevel", question.contentFilterLevel.name)
                
                val tagsArray = JSONArray()
                for (tag in question.tags) {
                    tagsArray.put(tag)
                }
                jsonObject.put("tags", tagsArray)
                
                jsonObject.put("isUserGenerated", true)
                jsonObject.put("createdBy", question.createdBy)
                jsonObject.put("createdAt", question.createdAt)
                
                val gameModeArray = JSONArray()
                for (gameMode in question.compatibleGameModes) {
                    gameModeArray.put(gameMode.name)
                }
                jsonObject.put("compatibleGameModes", gameModeArray)
                
                question.depthLevel?.let { jsonObject.put("depthLevel", it) }
                
                jsonArray.put(jsonObject)
            }
            
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // If there's an error, ignore it
        }
    }
}