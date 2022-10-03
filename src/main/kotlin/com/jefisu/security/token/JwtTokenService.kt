package com.jefisu.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtTokenService : TokenService {

    override suspend fun generate(config: TokenConfig, claim: TokenClaim): String {
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withExpiresAt(Date(config.expiredAt + System.currentTimeMillis()))
            .withClaim(claim.name, claim.value)
            .sign(Algorithm.HMAC512(config.secret))
    }
}