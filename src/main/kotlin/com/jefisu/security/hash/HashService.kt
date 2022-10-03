package com.jefisu.security.hash

interface HashService {
    fun generate(value: String): SaltedHash
    fun verify(value: String, saltedHash: SaltedHash): Boolean
}