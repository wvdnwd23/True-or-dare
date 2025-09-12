package com.wes.truthdare.core.data.repositories

import com.wes.truthdare.core.data.models.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing categories
 */
interface CategoryRepository {
    /**
     * Get all categories
     * @return List of all categories
     */
    suspend fun getAllCategories(): List<Category>
    
    /**
     * Get a category by ID
     * @param id The category ID
     * @return The category, or null if not found
     */
    suspend fun getCategoryById(id: String): Category?
    
    /**
     * Get categories by IDs
     * @param ids The category IDs
     * @return List of categories
     */
    suspend fun getCategoriesByIds(ids: List<String>): List<Category>
    
    /**
     * Get categories as a Flow
     * @return Flow of all categories
     */
    fun getCategoriesFlow(): Flow<List<Category>>
    
    /**
     * Insert or update a category
     * @param category The category to insert or update
     * @return The ID of the inserted or updated category
     */
    suspend fun insertOrUpdateCategory(category: Category): String
    
    /**
     * Insert or update multiple categories
     * @param categories The categories to insert or update
     */
    suspend fun insertOrUpdateCategories(categories: List<Category>)
    
    /**
     * Delete a category
     * @param categoryId The ID of the category to delete
     */
    suspend fun deleteCategory(categoryId: String)
    
    /**
     * Initialize the database with predefined categories
     */
    suspend fun initializePredefinedCategories()
}