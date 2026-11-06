package com.nordev.guardian.domain.pin

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    private val pin = "258036".toCharArray()

    @Test
    fun hashIsDeterministicForTheSameSalt() {
        val salt = ByteArray(PinHasher.SALT_BYTES) { it.toByte() }
        val first = PinHasher.hash(pin, salt, iterations = 1_000)
        val second = PinHasher.hash(pin, salt, iterations = 1_000)
        assertArrayEquals(first, second)
    }

    @Test
    fun differentSaltsProduceDifferentHashes() {
        val first = PinHasher.hash(pin, ByteArray(PinHasher.SALT_BYTES) { 1 }, iterations = 1_000)
        val second = PinHasher.hash(pin, ByteArray(PinHasher.SALT_BYTES) { 2 }, iterations = 1_000)
        assertFalse(first.contentEquals(second))
    }

    @Test
    fun verifyAcceptsOnlyTheMatchingPin() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash(pin, salt, iterations = 1_000)
        assertTrue(PinHasher.verify("258036".toCharArray(), hash, salt, 1_000))
        assertFalse(PinHasher.verify("258037".toCharArray(), hash, salt, 1_000))
    }

    @Test
    fun newSaltIsSixteenRandomBytes() {
        val first = PinHasher.newSalt()
        val second = PinHasher.newSalt()
        assertTrue(first.size == PinHasher.SALT_BYTES)
        assertFalse(first.contentEquals(second))
    }
}
