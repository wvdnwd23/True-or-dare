package com.wes.truthdare.core.di

import android.content.Context
import com.wes.truthdare.core.data.AppDatabase
import com.wes.truthdare.core.data.converters.RoomTypeConverters
import com.wes.truthdare.core.data.dao.GameSessionDao
import com.wes.truthdare.core.data.dao.JournalEntryDao
import com.wes.truthdare.core.data.dao.PlayerDao
import com.wes.truthdare.core.data.dao.PlayerPreferenceDao
import com.wes.truthdare.core.data.dao.QuestionHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing data-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    /**
     * Provide the Room database
     * @param context The application context
     * @return The Room database instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    /**
     * Provide the PlayerDao
     * @param database The Room database
     * @return The PlayerDao
     */
    @Provides
    @Singleton
    fun providePlayerDao(database: AppDatabase): PlayerDao {
        return database.playerDao()
    }
    
    /**
     * Provide the GameSessionDao
     * @param database The Room database
     * @return The GameSessionDao
     */
    @Provides
    @Singleton
    fun provideGameSessionDao(database: AppDatabase): GameSessionDao {
        return database.gameSessionDao()
    }
    
    /**
     * Provide the QuestionHistoryDao
     * @param database The Room database
     * @return The QuestionHistoryDao
     */
    @Provides
    @Singleton
    fun provideQuestionHistoryDao(database: AppDatabase): QuestionHistoryDao {
        return database.questionHistoryDao()
    }
    
    /**
     * Provide the PlayerPreferenceDao
     * @param database The Room database
     * @return The PlayerPreferenceDao
     */
    @Provides
    @Singleton
    fun providePlayerPreferenceDao(database: AppDatabase): PlayerPreferenceDao {
        return database.playerPreferenceDao()
    }
    
    /**
     * Provide the JournalEntryDao
     * @param database The Room database
     * @return The JournalEntryDao
     */
    @Provides
    @Singleton
    fun provideJournalEntryDao(database: AppDatabase): JournalEntryDao {
        return database.journalEntryDao()
    }
    
    /**
     * Provide the RoomTypeConverters
     * @return The RoomTypeConverters
     */
    @Provides
    @Singleton
    fun provideRoomTypeConverters(): RoomTypeConverters {
        return RoomTypeConverters()
    }
}