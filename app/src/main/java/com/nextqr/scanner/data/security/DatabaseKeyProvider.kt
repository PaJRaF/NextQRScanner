package com.nextqr.scanner.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supplies the SQLCipher passphrase for the encrypted scan database.
 *
 * The passphrase is a 256-bit random value generated once on first launch and
 * stored inside [EncryptedSharedPreferences], which is itself sealed by a key
 * held in the hardware-backed Android Keystore. The raw passphrase therefore
 * never appears in plaintext on disk and is never hard-coded in the binary.
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setRequestStrongBoxBacked(true)
            .build()
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** Returns the DB passphrase as bytes, generating+persisting it on first use. */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        prefs.getString(KEY_DB_PASSPHRASE, null)?.let {
            return Base64.decode(it, Base64.NO_WRAP)
        }
        val fresh = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(fresh, Base64.NO_WRAP))
            .apply()
        return fresh
    }

    fun securePrefs() = prefs

    private companion object {
        const val SECURE_PREFS_NAME = "secure_prefs"
        const val KEY_DB_PASSPHRASE = "db_passphrase"
    }
}
