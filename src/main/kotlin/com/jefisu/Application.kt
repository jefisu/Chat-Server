package com.jefisu

import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.data.data_source.UserDataSource
import com.jefisu.di.mainModule
import com.jefisu.plugins.*
import com.jefisu.security.token.TokenConfig
import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.koin

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    koin {
        modules(mainModule)
    }
    val config = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiredAt = 365L * 24L * 60L * 60L * 1000L,
        secret = System.getenv("secret")
    )
    val chatDataSource by inject<ChatDataSource>()
    val userDataSource by inject<UserDataSource>()

    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureSecurity(config, chatDataSource)
    configureRouting(config, userDataSource, chatDataSource)
}