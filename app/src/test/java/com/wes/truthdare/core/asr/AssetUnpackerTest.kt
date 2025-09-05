package com.wes.truthdare.core.asr

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

class AssetUnpackerTest {

    private lateinit var assetUnpacker: AssetUnpacker
    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var filesDir: File
    private lateinit var modelDir: File

    @Before
    fun setup() {
        context = mock(Context::class.java)
        assetManager = mock(AssetManager::class.java)
        
        // Create a temporary directory for testing
        filesDir = createTempDir("files")
        modelDir = File(filesDir, "models/nl")
        
        `when`(context.assets).thenReturn(assetManager)
        `when`(context.filesDir).thenReturn(filesDir)
        
        assetUnpacker = AssetUnpacker(context)
    }

    @Test
    fun `test isModelUnpacked returns false when files don't exist`() {
        // Given
        // No files exist in the model directory
        
        // When
        val result = assetUnpacker.isModelUnpacked()
        
        // Then
        assertFalse(result)
    }

    @Test
    fun `test isModelUnpacked returns true when all required files exist`() {
        // Given
        modelDir.mkdirs()
        File(modelDir, "final.conf").createNewFile()
        File(modelDir, "words.txt").createNewFile()
        File(modelDir, "am.model").createNewFile()
        
        // When
        val result = assetUnpacker.isModelUnpacked()
        
        // Then
        assertTrue(result)
    }

    @Test
    fun `test getModelPath returns correct path`() {
        // Given
        val expectedPath = File(filesDir, "models/nl").absolutePath
        
        // When
        val result = assetUnpacker.getModelPath()
        
        // Then
        assertEquals(expectedPath, result)
    }

    @Test
    fun `test unpackModel emits progress updates`() = runBlocking {
        // Given
        val modelFiles = arrayOf("final.conf", "words.txt", "am.model")
        `when`(assetManager.list("models/nl")).thenReturn(modelFiles)
        
        // Mock asset file content
        val dummyContent = "Dummy content".toByteArray()
        for (file in modelFiles) {
            `when`(assetManager.open("models/nl/$file"))
                .thenReturn(ByteArrayInputStream(dummyContent))
        }
        
        // When
        val progressUpdates = assetUnpacker.unpackModel().toList()
        
        // Then
        assertEquals(3, progressUpdates.size)
        assertEquals(1/3f, progressUpdates[0], 0.01f)
        assertEquals(2/3f, progressUpdates[1], 0.01f)
        assertEquals(3/3f, progressUpdates[2], 0.01f)
        
        // Verify files were created
        assertTrue(File(modelDir, "final.conf").exists())
        assertTrue(File(modelDir, "words.txt").exists())
        assertTrue(File(modelDir, "am.model").exists())
    }

    @Test
    fun `test cleanupModel removes model directory`() = runBlocking {
        // Given
        modelDir.mkdirs()
        File(modelDir, "final.conf").createNewFile()
        File(modelDir, "words.txt").createNewFile()
        File(modelDir, "am.model").createNewFile()
        
        assertTrue(modelDir.exists())
        
        // When
        assetUnpacker.cleanupModel()
        
        // Then
        assertFalse(modelDir.exists())
    }
}