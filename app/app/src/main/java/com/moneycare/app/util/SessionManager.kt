package com.moneycare.app.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _isLoggedIn = MutableStateFlow(prefs.contains(KEY_TOKEN))
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    companion object {
        private const val PREF_NAME = "moneycare_session"
        private const val KEY_TOKEN = "jwt_token_encrypted"
        private const val LEGACY_KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ALIAS = "moneycare_session_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    fun saveSession(token: String, userId: Long, userName: String) {
        val encryptedToken = encrypt(token)
        prefs.edit().apply {
            putString(KEY_TOKEN, encryptedToken)
            remove(LEGACY_KEY_TOKEN)
            putString(KEY_USER_ID, userId.toString())
            putString(KEY_USER_NAME, userName)
            apply()
        }
        _isLoggedIn.value = true
    }

    fun getToken(): String? {
        if (prefs.contains(LEGACY_KEY_TOKEN)) {
            prefs.edit().remove(LEGACY_KEY_TOKEN).apply()
        }
        val encryptedToken = prefs.getString(KEY_TOKEN, null) ?: return null
        return runCatching { decrypt(encryptedToken) }
            .getOrElse {
                clearSession()
                null
            }
    }

    fun getUserId(): Long = prefs.getString(KEY_USER_ID, "-1")?.toLongOrNull() ?: -1L

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun clearSession() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    fun isLoggedIn(): Boolean = getToken() != null

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = ByteBuffer.allocate(1 + cipher.iv.size + encrypted.size)
            .put(cipher.iv.size.toByte())
            .put(cipher.iv)
            .put(encrypted)
            .array()
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val payload = ByteBuffer.wrap(Base64.decode(value, Base64.NO_WRAP))
        val ivSize = payload.get().toInt() and 0xFF
        require(ivSize in 12..16 && payload.remaining() > ivSize) { "Invalid encrypted session" }
        val iv = ByteArray(ivSize).also(payload::get)
        val encrypted = ByteArray(payload.remaining()).also(payload::get)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }
}
