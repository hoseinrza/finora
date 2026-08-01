package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Owns the passphrase used to encrypt the local SQLCipher-backed Room database.
 *
 * The passphrase is a random 256-bit value generated once on first launch. It is never
 * hardcoded and never leaves the device. At rest it is protected by [EncryptedSharedPreferences],
 * whose own encryption key is generated and held inside the Android Keystore (hardware-backed
 * on devices that support it) via [MasterKey] - so the passphrase is never written to disk in
 * plaintext, and it is not extractable without the device's Keystore-backed key.
 */
object DatabasePassphraseProvider {

    private const val PREFS_FILE_NAME = "finora_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase_b64"
    private const val PASSPHRASE_BYTE_LENGTH = 32 // 256-bit, matches SQLCipher's default cipher strength

    /**
     * Returns the existing database passphrase, generating and persisting a new one on first call.
     */
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = encryptedPrefs(context)
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }

        val newPassphrase = ByteArray(PASSPHRASE_BYTE_LENGTH).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(newPassphrase, Base64.NO_WRAP))
            .apply()
        return newPassphrase
    }

    private fun encryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
