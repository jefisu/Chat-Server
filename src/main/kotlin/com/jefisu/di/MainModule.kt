package com.jefisu.di

import com.jefisu.data.chat.ChatController
import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.data.data_source.UserDataSource
import com.jefisu.data.database.MongoChatDataSource
import com.jefisu.data.database.MongoUserDataSource
import com.jefisu.security.hash.HashService
import com.jefisu.security.hash.SHA256HashService
import com.jefisu.security.token.JwtTokenService
import com.jefisu.security.token.TokenService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


val mainModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) { json() }
        }
    }
    single {
        KMongo.createClient()
            .coroutine
            .getDatabase("chat_db")
    }
    single<UserDataSource> {
        MongoUserDataSource(get())
    }
    single<HashService> {
        SHA256HashService()
    }
    single<TokenService> {
        JwtTokenService()
    }
    single<ChatDataSource> {
        MongoChatDataSource(get())
    }
    single {
        ChatController(get())
    }
}