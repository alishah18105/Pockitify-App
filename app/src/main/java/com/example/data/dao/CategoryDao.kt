package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type OR type = 'BOTH' ORDER BY name ASC")
    fun getCategoriesByTypeFlow(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE LOWER(name) = LOWER(:name) AND (type = :type OR type = 'BOTH') LIMIT 1")
    suspend fun findCategoryByNameAndType(name: String, type: String): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE id NOT IN (SELECT MIN(id) FROM categories GROUP BY LOWER(name), type)")
    suspend fun removeDuplicateCategories()

    @Query("DELETE FROM categories WHERE LOWER(name) = 'cat food'")
    suspend fun removeCatFoodCategory()

    @Query("DELETE FROM categories WHERE LOWER(name) IN ('medicines', 'medicine', 'monthly expenditure', 'bird food', 'birds', 'cat food')")
    suspend fun removeDisallowedExpenseCategories()
}
