package com.wes.truthdare.core.asr

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class responsible for unpacking ASR model assets to the file system
 */
@Singleton
class AssetUnpacker @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "AssetUnpacker"
        private const val MODEL_DIR = "models/nl"
        private const val TARGET_DIR = "models/nl"
    }
    
    /**
     * Check if the model is already unpacked
     * @return True if the model is unpacked, false otherwise
     */
    fun isModelUnpacked(): Boolean {
        val targetDir = File(context.filesDir, TARGET_DIR)
        if (!targetDir.exists()) return false
        
        // Check for essential model files
        val confFile = File(targetDir, "final.conf")
        val vocabFile = File(targetDir, "words.txt")
        val modelFile = File(targetDir, "am.model")
        
        return confFile.exists() && vocabFile.exists() && modelFile.exists()
    }
    
    /**
     * Unpack the model assets to the file system
     * @return A flow of progress updates (0.0 to 1.0)
     */
    fun unpackModel(): Flow<Float> = flow {
        val assetManager = context.assets
        val targetDir = File(context.filesDir, TARGET_DIR).apply {
            mkdirs()
        }
        
        try {
            // List all files in the model directory
            val files = assetManager.list(MODEL_DIR) ?: emptyArray()
            val totalFiles = files.size
            var processedFiles = 0
            
            // Copy each file
            for (file in files) {
                val sourceFile = "$MODEL_DIR/$file"
                val targetFile = File(targetDir, file)
                
                assetManager.open(sourceFile).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                    }
                }
                
                processedFiles++
                emit(processedFiles.toFloat() / totalFiles)
            }
            
            Log.d(TAG, "Model unpacked successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error unpacking model", e)
            throw e
        }
    }
    
    /**
     * Get the path to the unpacked model directory
     * @return The path to the model directory
     */
    fun getModelPath(): String {
        return File(context.filesDir, TARGET_DIR).absolutePath
    }
    
    /**
     * Clean up the unpacked model files
     */
    suspend fun cleanupModel() = withContext(Dispatchers.IO) {
        val targetDir = File(context.filesDir, TARGET_DIR)
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
            Log.d(TAG, "Model cleaned up successfully")
        }
    }
}