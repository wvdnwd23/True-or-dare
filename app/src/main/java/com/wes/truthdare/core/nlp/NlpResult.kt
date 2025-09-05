package com.wes.truthdare.core.nlp

/**
 * Data class representing the result of NLP processing
 */
data class NlpResult(
    val tags: List<String>,
    val intent: String?,
    val sentiment: Int,  // -100 to 100, negative to positive
    val triggered: Boolean
)