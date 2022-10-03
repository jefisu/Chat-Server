package com.jefisu.security.token

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val expiredAt: Long,
    val secret: String
)