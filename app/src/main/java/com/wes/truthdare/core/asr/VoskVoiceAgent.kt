package com.wes.truthdare.core.asr

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.wes.truthdare.core.agents.VoiceAgent
import com.wes.truthdare.core.util.Prosody
import com.wes.truthdare.core.util.SilencePeriod
import com.wes.truthdare.core.util.TranscriptEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VoiceAgent using Vosk for offline speech recognition
 */
@Singleton
class VoskVoiceAgent @Inject constructor(
    private val context: Context,
    private val assetUnpacker: AssetUnpacker
) : VoiceAgent {
    companion object {
        private const val TAG = "VoskVoiceAgent"
        private const val SAMPLE_RATE = 16000
    }
    
    private var speechService: SpeechService? = null
    private var model: Model? = null
    private val isListening = AtomicBoolean(false)
    
    // Prosody tracking
    private var speechStartTime = 0L
    private var wordCount = 0
    private var silencePeriods = mutableListOf<SilencePeriod>()
    private var lastSpeechTime = 0L
    private var pitchSum = 0f
    private var pitchCount = 0
    
    /**
     * Initialize the Vosk model
     * @throws Exception if the model cannot be initialized
     */
    private fun initializeModel() {
        if (model == null) {
            try {
                val modelPath = assetUnpacker.getModelPath()
                model = Model(modelPath)
                Log.d(TAG, "Vosk model initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Vosk model", e)
                throw e
            }
        }
    }
    
    /**
     * Start listening for voice input
     * @return Flow of TranscriptEvent containing recognized text and metadata
     */
    override fun startListening(): Flow<TranscriptEvent> = callbackFlow {
        if (isListening.getAndSet(true)) {
            close(IllegalStateException("Already listening"))
            return@callbackFlow
        }
        
        try {
            // Initialize model if needed
            initializeModel()
            
            // Reset prosody tracking
            speechStartTime = System.currentTimeMillis()
            wordCount = 0
            silencePeriods.clear()
            lastSpeechTime = speechStartTime
            pitchSum = 0f
            pitchCount = 0
            
            // Create recognizer
            val recognizer = Recognizer(
                model,
                SAMPLE_RATE.toFloat(),
                "[&quot;one&quot;, &quot;two&quot;, &quot;three&quot;, &quot;four&quot;, &quot;five&quot;, &quot;six&quot;, &quot;seven&quot;, &quot;eight&quot;, &quot;nine&quot;, &quot;zero&quot;, &quot;oh&quot;]"
            )
            
            // Create audio recording configuration
            val audioRecordingConfig = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .build()
            
            // Create recognition listener
            val listener = object : RecognitionListener {
                override fun onPartialResult(hypothesis: String) {
                    try {
                        val json = JSONObject(hypothesis)
                        val partial = json.optString("partial", "")
                        
                        if (partial.isNotEmpty()) {
                            // Update prosody data
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastSpeechTime > 1000) {
                                // Detected silence period
                                silencePeriods.add(
                                    SilencePeriod(
                                        lastSpeechTime - speechStartTime,
                                        currentTime - lastSpeechTime
                                    )
                                )
                            }
                            lastSpeechTime = currentTime
                            
                            // Count words for speech rate calculation
                            wordCount = partial.split(" ").size
                            
                            // Simulate pitch detection (in a real app, this would use actual audio analysis)
                            val simulatedPitch = 0.5f + (Math.random() * 0.3f).toFloat()
                            pitchSum += simulatedPitch
                            pitchCount++
                            
                            // Create prosody data
                            val prosody = createProsody()
                            
                            // Emit partial result
                            trySend(
                                TranscriptEvent(
                                    text = partial,
                                    confidence = 0.5f, // Partial results have lower confidence
                                    prosody = prosody
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing partial result", e)
                    }
                }
                
                override fun onResult(hypothesis: String) {
                    try {
                        val json = JSONObject(hypothesis)
                        val text = json.optString("text", "")
                        
                        if (text.isNotEmpty()) {
                            // Update prosody data
                            val currentTime = System.currentTimeMillis()
                            lastSpeechTime = currentTime
                            
                            // Count words for speech rate calculation
                            wordCount = text.split(" ").size
                            
                            // Create prosody data
                            val prosody = createProsody()
                            
                            // Emit final result
                            trySend(
                                TranscriptEvent(
                                    text = text,
                                    confidence = 0.8f, // Final results have higher confidence
                                    prosody = prosody
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing final result", e)
                    }
                }
                
                override fun onFinalResult(hypothesis: String) {
                    // Similar to onResult but called when recognition is complete
                    try {
                        val json = JSONObject(hypothesis)
                        val text = json.optString("text", "")
                        
                        if (text.isNotEmpty()) {
                            // Create final prosody data
                            val prosody = createProsody()
                            
                            // Emit final result
                            trySend(
                                TranscriptEvent(
                                    text = text,
                                    confidence = 0.9f, // Final results have highest confidence
                                    prosody = prosody
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing final result", e)
                    }
                }
                
                override fun onError(exception: Exception) {
                    Log.e(TAG, "Recognition error", exception)
                    close(exception)
                }
                
                override fun onTimeout() {
                    Log.d(TAG, "Recognition timeout")
                    close()
                }
            }
            
            // Start speech service
            speechService = SpeechService(recognizer, SAMPLE_RATE.toFloat())
            speechService?.startListening(audioRecordingConfig, listener)
            
            Log.d(TAG, "Started listening")
            
            // Clean up when flow is cancelled
            awaitClose {
                speechService?.stop()
                speechService = null
                isListening.set(false)
                Log.d(TAG, "Stopped listening")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            isListening.set(false)
            close(e)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Stop listening for voice input
     */
    override fun stopListening() {
        if (isListening.getAndSet(false)) {
            speechService?.stop()
            speechService = null
            Log.d(TAG, "Stopped listening")
        }
    }
    
    /**
     * Create prosody data from current speech metrics
     * @return Prosody object with current speech characteristics
     */
    private fun createProsody(): Prosody {
        val currentTime = System.currentTimeMillis()
        val duration = (currentTime - speechStartTime) / 1000f // in seconds
        
        // Calculate speech rate (words per second)
        val rate = if (duration > 0) wordCount / duration else 0f
        
        // Calculate average pitch
        val pitch = if (pitchCount > 0) pitchSum / pitchCount else 0.5f
        
        return Prosody(
            pitch = pitch,
            rate = rate,
            silences = silencePeriods.toList() // Create a copy of the list
        )
    }
}