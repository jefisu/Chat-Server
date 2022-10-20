package com.jefisu.plugins

import com.jefisu.data.chat.ChatController
import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.data.data_source.UserDataSource
import com.jefisu.routes.*
import com.jefisu.security.hash.HashService
import com.jefisu.security.token.TokenConfig
import com.jefisu.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting(
    config: TokenConfig,
    userDataSource: UserDataSource,
    chatDataSource: ChatDataSource
) {
    val tokenService by inject<TokenService>()
    val hashService by inject<HashService>()
    val chatController by inject<ChatController>()

    routing {
        static {
            resources("static")
        }

        auth(userDataSource)
        signIn(userDataSource, hashService, tokenService, config)
        signUp(userDataSource, hashService)
        chatSocket(chatController)
        getChat(chatDataSource)
        changeUserInfo(userDataSource)
        getUser(userDataSource)
        changePassword(userDataSource, hashService)
        changeAvatar(userDataSource)
        chatsByUser(chatDataSource)
        getAllUsers(userDataSource)
        deleteChat(chatDataSource)
        deleteMessage(chatDataSource)
        clearChat(chatDataSource)
    }
}
