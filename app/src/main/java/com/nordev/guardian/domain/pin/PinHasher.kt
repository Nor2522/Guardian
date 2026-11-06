package com.nordev.guardian.domain.pin

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// PBKDF2-HMAC-SHA256. The PIN is never stored; only this derived hash and its
// random salt are. Inputs are cleared after use.
object PinHasher {
    const val ITERATIONS = 210_000
    const val SALT_BYTES = 16
    private const val KEY_BITS = 256

    fun newSalt(): ByteArray = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }

    fun hash(pin: CharArray, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val spec = PBEKeySpec(pin, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(pin: CharArray, expected: ByteArray, salt: ByteArray, iterations: Int): Boolean {
        val actual = hash(pin, salt, iterations)
        return try {
            MessageDigest.isEqual(actual, expected)
        } finally {
            actual.fill(0)
        }
    }
}
