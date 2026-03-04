package org.example.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.util.*

class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationTime: Long = 86400000 // 24 hours
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun verifier(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: String, email: String, role: String): String {
        return JWT.create()
            .withSubject(userId)
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("email", email)
            .withClaim("role", role)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + expirationTime))
            .sign(algorithm)
    }
}