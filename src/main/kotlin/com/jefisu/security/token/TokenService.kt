package com.jefisu.security.token

interface TokenService {
    suspend fun generate(config: TokenConfig, claim: TokenClaim): String
}