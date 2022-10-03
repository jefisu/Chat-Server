package com.jefisu.plugins

import com.auth0.jwt.algorithms.Algorithm
import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.data.data_source.UserDataSource
import com.jefisu.data.mapper.toUserDto
import com.jefisu.security.token.TokenConfig
import com.jefisu.session.ChatSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

fun Application.configureSecurity(
    config: TokenConfig,
    chatDataSource: ChatDataSource,
    userDataSource: UserDataSource
) {
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

    intercept(ApplicationCallPipeline.Plugins) {
        if (call.sessions.get<ChatSession>() == null) {
            val senderUsername = call.parameters["sender"] ?: return@intercept
            val recipientUsername = call.parameters["recipient"] ?: return@intercept

            val sender = userDataSource.getUserByLogin(senderUsername)
            val recipient = userDataSource.getUserByLogin(recipientUsername)

            val chat = chatDataSource.startNewChat(
                sender?.toUserDto() ?: return@intercept,
                recipient?.toUserDto() ?: return@intercept
            )
            call.sessions.set(ChatSession(sender, recipient, chat))
        }
    }


    authentication {
        jwt {
            realm = this@configureSecurity.environment.config.property("jwt.realm").toString()
            verifier(
                issuer = config.issuer,
                audience = config.audience,
                algorithm = Algorithm.HMAC512(config.secret)
            )
            validate { credential ->
                if (credential.payload.audience.contains(config.audience)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(
                    message = "This token is invalid.",
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}