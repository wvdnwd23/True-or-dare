package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.models.SubOption
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing sub-options
 */
interface SubOptionRepository {
    /**
     * Get all sub-options
     * @return List of all sub-options
     */
    suspend fun getAllSubOptions(): List<SubOption>
    
    /**
     * Get sub-options for a specific category
     * @param categoryId The category ID
     * @return List of sub-options for the category
     */
    suspend fun getSubOptionsForCategory(categoryId: String): List<SubOption>
    
    /**
     * Get a sub-option by ID
     * @param id The sub-option ID
     * @return The sub-option, or null if not found
     */
    suspend fun getSubOptionById(id: String): SubOption?
    
    /**
     * Get sub-options by IDs
     * @param ids The sub-option IDs
     * @return List of sub-options
     */
    suspend fun getSubOptionsByIds(ids: List<String>): List<SubOption>
    
    /**
     * Get sub-options as a Flow
     * @return Flow of all sub-options
     */
    fun getSubOptionsFlow(): Flow<List<SubOption>>
    
    /**
     * Get sub-options for a specific category as a Flow
     * @param categoryId The category ID
     * @return Flow of sub-options for the category
     */
    fun getSubOptionsForCategoryFlow(categoryId: String): Flow<List<SubOption>>
    
    /**
     * Insert or update a sub-option
     * @param subOption The sub-option to insert or update
     * @return The ID of the inserted or updated sub-option
     */
    suspend fun insertOrUpdateSubOption(subOption: SubOption): String
    
    /**
     * Insert or update multiple sub-options
     * @param subOptions The sub-options to insert or update
     */
    suspend fun insertOrUpdateSubOptions(subOptions: List<SubOption>)
    
    /**
     * Delete a sub-option
     * @param subOptionId The ID of the sub-option to delete
     */
    suspend fun deleteSubOption(subOptionId: String)
    
    /**
     * Update the question count for a sub-option
     * @param subOptionId The sub-option ID
     * @param count The new question count
     */
    suspend fun updateQuestionCount(subOptionId: String, count: Int)
    
    /**
     * Initialize the database with predefined sub-options
     */
    suspend fun initializePredefinedSubOptions()
}