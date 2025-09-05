package com.wes.truthdare.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for cryptographic operations
 */
@Singleton
class CryptoManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "truth_dare_master_key"
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val ENCRYPTION_IV_LENGTH = 12
        private const val ENCRYPTION_TAG_LENGTH = 128
    }
    
    /**
     * Get or create the master key
     * @return The master key
     */
    private fun getMasterKey(): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    /**
     * Create an encrypted file
     * @param file The file to encrypt
     * @return An EncryptedFile instance
     */
    fun createEncryptedFile(file: File): EncryptedFile {
        return EncryptedFile.Builder(
            context,
            file,
            getMasterKey(),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }
    
    /**
     * Encrypt data
     * @param data The data to encrypt
     * @return The encrypted data
     */
    fun encrypt(data: ByteArray): ByteArray {
        val key = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        
        // Combine IV and encrypted data
        return iv + encrypted
    }
    
    /**
     * Decrypt data
     * @param data The data to decrypt
     * @return The decrypted data
     */
    fun decrypt(data: ByteArray): ByteArray {
        val key = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
        
        // Extract IV from the beginning of the data
        val iv = data.copyOfRange(0, ENCRYPTION_IV_LENGTH)
        val encrypted = data.copyOfRange(ENCRYPTION_IV_LENGTH, data.size)
        
        val spec = GCMParameterSpec(ENCRYPTION_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        return cipher.doFinal(encrypted)
    }
    
    /**
     * Encrypt a string
     * @param text The string to encrypt
     * @return The encrypted string as a Base64-encoded string
     */
    fun encryptString(text: String): String {
        val encrypted = encrypt(text.toByteArray())
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
    }
    
    /**
     * Decrypt a string
     * @param encryptedText The Base64-encoded encrypted string
     * @return The decrypted string
     */
    fun decryptString(encryptedText: String): String {
        val data = android.util.Base64.decode(encryptedText, android.util.Base64.DEFAULT)
        val decrypted = decrypt(data)
        return String(decrypted)
    }
    
    /**
     * Write encrypted data to a file
     * @param file The file to write to
     * @param data The data to encrypt and write
     */
    fun writeEncrypted(file: File, data: ByteArray) {
        val encryptedFile = createEncryptedFile(file)
        encryptedFile.openFileOutput().use { output ->
            output.write(data)
        }
    }
    
    /**
     * Read encrypted data from a file
     * @param file The file to read from
     * @return The decrypted data
     */
    fun readEncrypted(file: File): ByteArray {
        val encryptedFile = createEncryptedFile(file)
        return encryptedFile.openFileInput().use { input ->
            input.readBytes()
        }
    }
    
    /**
     * Get or create a secret key for encryption
     * @return The secret key
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
        }
        
        val keyGenerator = KeyGenerator.getInstance(
            ENCRYPTION_ALGORITHM,
            KEYSTORE_PROVIDER
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(ENCRYPTION_BLOCK_MODE)
            .setEncryptionPaddings(ENCRYPTION_PADDING)
            .setKeySize(256)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}