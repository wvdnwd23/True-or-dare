package com.wes.truthdare.core.agents

import com.wes.truthdare.core.util.TranscriptEvent
import kotlinx.coroutines.flow.Flow

/**
 * Interface for voice recognition functionality
 */
interface VoiceAgent {
    /**
     * Start listening for voice input
     * @return Flow of TranscriptEvent containing recognized text and metadata
     */
    fun startListening(): Flow<TranscriptEvent>
    
    /**
     * Stop listening for voice input
     */
    fun stopListening()
}