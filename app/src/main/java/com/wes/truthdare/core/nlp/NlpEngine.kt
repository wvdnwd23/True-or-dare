package com.wes.truthdare.core.nlp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main NLP engine that combines all NLP components
 */
@Singleton
class NlpEngine @Inject constructor(
    private val tagger: Tagger,
    private val intentDetector: IntentDetector,
    private val sentimentAnalyzer: SentimentAnalyzer,
    private val triggerScanner: TriggerScanner
) {
    /**
     * Analyze text using all NLP components
     * @param text The text to analyze
     * @return An NlpResult containing tags, intent, sentiment, and trigger status
     */
    suspend fun analyze(text: String): NlpResult = withContext(Dispatchers.Default) {
        // Run all analyses in parallel
        val tagsDeferred = async { tagger.extractTags(text) }
        val intentDeferred = async { intentDetector.detectIntent(text) }
        val sentimentDeferred = async { sentimentAnalyzer.analyzeSentiment(text) }
        val triggeredDeferred = async { triggerScanner.scanForTriggers(text) }
        
        // Wait for all analyses to complete
        val (tags, intent, sentiment, triggered) = awaitAll(
            tagsDeferred, intentDeferred, sentimentDeferred, triggeredDeferred
        )
        
        NlpResult(
            tags = tags as List<String>,
            intent = intent as String?,
            sentiment = sentiment as Int,
            triggered = triggered as Boolean
        )
    }
}