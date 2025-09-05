package com.wes.truthdare.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wes.truthdare.core.data.converters.RoomTypeConverters
import com.wes.truthdare.core.data.dao.GameSessionDao
import com.wes.truthdare.core.data.dao.JournalEntryDao
import com.wes.truthdare.core.data.dao.PlayerDao
import com.wes.truthdare.core.data.dao.PlayerPreferenceDao
import com.wes.truthdare.core.data.dao.QuestionHistoryDao
import com.wes.truthdare.core.data.entities.GameSessionEntity
import com.wes.truthdare.core.data.entities.JournalEntryEntity
import com.wes.truthdare.core.data.entities.PlayerEntity
import com.wes.truthdare.core.data.entities.PlayerPreferenceEntity
import com.wes.truthdare.core.data.entities.QuestionHistoryEntity

/**
 * Main Room database for the application
 */
@Database(
    entities = [
        PlayerEntity::class,
        GameSessionEntity::class,
        QuestionHistoryEntity::class,
        PlayerPreferenceEntity::class,
        JournalEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Get the PlayerDao
     */
    abstract fun playerDao(): PlayerDao
    
    /**
     * Get the GameSessionDao
     */
    abstract fun gameSessionDao(): GameSessionDao
    
    /**
     * Get the QuestionHistoryDao
     */
    abstract fun questionHistoryDao(): QuestionHistoryDao
    
    /**
     * Get the PlayerPreferenceDao
     */
    abstract fun playerPreferenceDao(): PlayerPreferenceDao
    
    /**
     * Get the JournalEntryDao
     */
    abstract fun journalEntryDao(): JournalEntryDao
    
    companion object {
        private const val DATABASE_NAME = "truth_dare_db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Get the database instance
         * @param context The application context
         * @return The database instance
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}