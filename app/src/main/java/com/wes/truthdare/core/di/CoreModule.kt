package com.wes.truthdare.core.di

import android.content.Context
import com.wes.truthdare.core.agents.EmotionAgent
import com.wes.truthdare.core.agents.LearningAgent
import com.wes.truthdare.core.agents.QuestionAgent
import com.wes.truthdare.core.agents.SafetyAgent
import com.wes.truthdare.core.agents.StoryAgent
import com.wes.truthdare.core.agents.VoiceAgent
import com.wes.truthdare.core.asr.AssetUnpacker
import com.wes.truthdare.core.asr.VoskVoiceAgent
import com.wes.truthdare.core.data.repositories.GameSessionRepository
import com.wes.truthdare.core.data.repositories.JournalRepository
import com.wes.truthdare.core.data.repositories.PlayerPreferenceRepository
import com.wes.truthdare.core.data.repositories.PlayerRepository
import com.wes.truthdare.core.data.repositories.QuestionHistoryRepository
import com.wes.truthdare.core.data.repositories.QuestionRepository
import com.wes.truthdare.core.impl.DefaultEmotionAgent
import com.wes.truthdare.core.impl.DefaultLearningAgent
import com.wes.truthdare.core.impl.DefaultQuestionAgent
import com.wes.truthdare.core.impl.DefaultSafetyAgent
import com.wes.truthdare.core.impl.DefaultStoryAgent
import com.wes.truthdare.core.nlp.IntentDetector
import com.wes.truthdare.core.nlp.NlpEngine
import com.wes.truthdare.core.nlp.SentimentAnalyzer
import com.wes.truthdare.core.nlp.Tagger
import com.wes.truthdare.core.nlp.TriggerScanner
import com.wes.truthdare.core.security.CryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing core component dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    /**
     * Provide the AssetUnpacker
     * @param context The application context
     * @return The AssetUnpacker
     */
    @Provides
    @Singleton
    fun provideAssetUnpacker(@ApplicationContext context: Context): AssetUnpacker {
        return AssetUnpacker(context)
    }
    
    /**
     * Provide the VoiceAgent implementation
     * @param context The application context
     * @param assetUnpacker The AssetUnpacker
     * @return The VoiceAgent implementation
     */
    @Provides
    @Singleton
    fun provideVoiceAgent(
        @ApplicationContext context: Context,
        assetUnpacker: AssetUnpacker
    ): VoiceAgent {
        return VoskVoiceAgent(context, assetUnpacker)
    }
    
    /**
     * Provide the EmotionAgent implementation
     * @param nlpEngine The NLP engine
     * @return The EmotionAgent implementation
     */
    @Provides
    @Singleton
    fun provideEmotionAgent(nlpEngine: NlpEngine): EmotionAgent {
        return DefaultEmotionAgent(nlpEngine)
    }
    
    /**
     * Provide the QuestionAgent implementation
     * @param questionRepository The QuestionRepository
     * @param nlpEngine The NLP engine
     * @return The QuestionAgent implementation
     */
    @Provides
    @Singleton
    fun provideQuestionAgent(
        questionRepository: QuestionRepository,
        nlpEngine: NlpEngine
    ): QuestionAgent {
        return DefaultQuestionAgent(questionRepository, nlpEngine)
    }
    
    /**
     * Provide the SafetyAgent implementation
     * @param nlpEngine The NLP engine
     * @param triggerScanner The TriggerScanner
     * @return The SafetyAgent implementation
     */
    @Provides
    @Singleton
    fun provideSafetyAgent(
        nlpEngine: NlpEngine,
        triggerScanner: TriggerScanner
    ): SafetyAgent {
        return DefaultSafetyAgent(nlpEngine, triggerScanner)
    }
    
    /**
     * Provide the StoryAgent implementation
     * @param questionHistoryRepository The QuestionHistoryRepository
     * @param nlpEngine The NLP engine
     * @return The StoryAgent implementation
     */
    @Provides
    @Singleton
    fun provideStoryAgent(
        questionHistoryRepository: QuestionHistoryRepository,
        nlpEngine: NlpEngine
    ): StoryAgent {
        return DefaultStoryAgent(questionHistoryRepository, nlpEngine)
    }
    
    /**
     * Provide the LearningAgent implementation
     * @param playerPreferenceRepository The PlayerPreferenceRepository
     * @return The LearningAgent implementation
     */
    @Provides
    @Singleton
    fun provideLearningAgent(
        playerPreferenceRepository: PlayerPreferenceRepository
    ): LearningAgent {
        return DefaultLearningAgent(playerPreferenceRepository)
    }
    
    /**
     * Provide the CryptoManager
     * @param context The application context
     * @return The CryptoManager
     */
    @Provides
    @Singleton
    fun provideCryptoManager(@ApplicationContext context: Context): CryptoManager {
        return CryptoManager(context)
    }
}