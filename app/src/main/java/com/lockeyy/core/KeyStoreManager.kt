package com.lockeyy.core

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyStoreManager @Inject constructor() {

    private val KEY_OP_MODE = "AndroidKeyStore"
    private val KEY_ALIAS = "LockeyyMasterKey"
    private val TRANSFORMATION = "AES/GCM/NoPadding"

    init {
        createKey()
    }

    private fun createKey() {
        val keyStore = KeyStore.getInstance(KEY_OP_MODE)
        keyStore.load(null)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_OP_MODE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    fun encrypt(data: String): String {
        val keyStore = KeyStore.getInstance(KEY_OP_MODE)
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryption = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // Combine IV and encrypted data: IV + Data
        val combined = ByteArray(iv.size + encryption.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryption, 0, combined, iv.size, encryption.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String): String {
        val keyStore = KeyStore.getInstance(KEY_OP_MODE)
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        
        // Extract IV (GCM standard IV length is 12 bytes)
        val iv = ByteArray(12)
        System.arraycopy(combined, 0, iv, 0, 12)
        
        val encryptedContent = ByteArray(combined.size - 12)
        System.arraycopy(combined, 12, encryptedContent, 0, encryptedContent.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val decoded = cipher.doFinal(encryptedContent)
        return String(decoded, Charsets.UTF_8)
    }
}
