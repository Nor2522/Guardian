package com.nordev.guardian.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nordev.guardian.domain.pin.PinHasher
import com.nordev.guardian.domain.pin.PinPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Base64

private val Context.parentPinStore by preferencesDataStore(name = "parent_pin")

data class ParentPinState(
    val hasPin: Boolean,
    val failedAttempts: Int,
    val lockedUntil: Long,
)

sealed interface PinVerifyResult {
    data object Success : PinVerifyResult
    data object Wrong : PinVerifyResult
    data object NoPin : PinVerifyResult
    data class Locked(val remainingMillis: Long) : PinVerifyResult
    data object Failure : PinVerifyResult
}

// Stores only the PBKDF2 hash, salt, iteration count, and lockout counters.
// The PIN itself, and the derived hash, never leave this class in plain form.
class ParentPinRepository(context: Context) {
    private val store = context.applicationContext.parentPinStore

    suspend fun state(): ParentPinState {
        val prefs = store.data.first()
        return ParentPinState(
            hasPin = prefs[HASH] != null && prefs[SALT] != null,
            failedAttempts = prefs[FAILED_ATTEMPTS] ?: 0,
            lockedUntil = prefs[LOCKED_UNTIL] ?: 0L,
        )
    }

    suspend fun setPin(pin: String) {
        val salt = PinHasher.newSalt()
        val chars = pin.toCharArray()
        val hash = try {
            withContext(Dispatchers.Default) { PinHasher.hash(chars, salt) }
        } finally {
            chars.fill('\u0000')
        }
        try {
            store.edit { prefs ->
                prefs[HASH] = Base64.getEncoder().encodeToString(hash)
                prefs[SALT] = Base64.getEncoder().encodeToString(salt)
                prefs[ITERATIONS] = PinHasher.ITERATIONS
                prefs[FAILED_ATTEMPTS] = 0
                prefs[LOCKED_UNTIL] = 0L
            }
        } finally {
            hash.fill(0)
        }
    }

    suspend fun verify(pin: String, now: Long = System.currentTimeMillis()): PinVerifyResult {
        val prefs = store.data.first()
        val lockedUntil = prefs[LOCKED_UNTIL] ?: 0L
        if (lockedUntil > now) {
            return PinVerifyResult.Locked(lockedUntil - now)
        }

        val hash = prefs[HASH]?.let { decode(it) } ?: return PinVerifyResult.NoPin
        val salt = prefs[SALT]?.let { decode(it) } ?: run {
            hash.fill(0)
            return PinVerifyResult.NoPin
        }
        val iterations = prefs[ITERATIONS] ?: PinHasher.ITERATIONS
        val chars = pin.toCharArray()
        val matches = try {
            withContext(Dispatchers.Default) {
                PinHasher.verify(chars, hash, salt, iterations)
            }
        } catch (error: Exception) {
            return PinVerifyResult.Failure
        } finally {
            chars.fill('\u0000')
            hash.fill(0)
            salt.fill(0)
        }

        if (matches) {
            store.edit { prefs ->
                prefs[FAILED_ATTEMPTS] = 0
                prefs[LOCKED_UNTIL] = 0L
            }
            return PinVerifyResult.Success
        }

        val next = PinPolicy.nextFailure(prefs[FAILED_ATTEMPTS] ?: 0, now)
        store.edit { prefs ->
            prefs[FAILED_ATTEMPTS] = next.failedAttempts
            prefs[LOCKED_UNTIL] = next.lockedUntil
        }
        return if (next.lockedUntil > now) {
            PinVerifyResult.Locked(next.lockedUntil - now)
        } else {
            PinVerifyResult.Wrong
        }
    }

    private fun decode(value: String): ByteArray? =
        runCatching { Base64.getDecoder().decode(value) }.getOrNull()

    private companion object {
        val HASH = stringPreferencesKey("hash")
        val SALT = stringPreferencesKey("salt")
        val ITERATIONS = intPreferencesKey("iterations")
        val FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        val LOCKED_UNTIL = longPreferencesKey("locked_until")
    }
}
