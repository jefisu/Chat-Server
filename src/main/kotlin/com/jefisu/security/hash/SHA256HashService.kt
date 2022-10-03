package com.jefisu.security.hash

import io.ktor.util.*
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256HashService : HashService {

    override fun generate(value: String): SaltedHash {
        val saltAsHex = SecureRandom.getInstance("SHA1PRNG").generateSeed(32)
        val salt = hex(saltAsHex)
        val hash = DigestUtils.sha256Hex("$salt$value")
        return SaltedHash(hash, salt)
    }

    override fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return saltedHash.hash == DigestUtils.sha256Hex(saltedHash.salt + value)
    }
}