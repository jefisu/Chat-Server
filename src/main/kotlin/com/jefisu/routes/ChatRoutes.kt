package com.jefisu.routes

import com.jefisu.data.chat.ChatController
import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.data.model.response.UserDto
import com.jefisu.session.ChatSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach

fun Route.chatSocket(
    chatController: ChatController
) {
    webSocket("chat-socket") {
        val session = call.sessions.get<ChatSession>() ?: kotlin.run {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }
        val joinChat = chatController.joinChat(
            session.ownerUser.username,
            session.chat.id,
            this
        )
        if (!joinChat) {
            webSocketRaw {
                chatController.joinChat(
                    session.ownerUser.username,
                    session.chat.id,
                    this
                )
            }
        }
        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    chatController.sendMessage(
                        chat = session.chat,
                        ownerUser = session.ownerUser,
                        text = frame.readText()
                    )
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            e.printStackTrace()
        } finally {
            chatController.exitChat(session.ownerUser.username)
        }
    }
}

fun Route.chatsByUser(chatDataSource: ChatDataSource) {
    post("chats") {
        val user = call.receiveNullable<UserDto>() ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "User not registered"
            )
            return@post
        }
        val chats = chatDataSource.getChatsByUser(user)
        call.respond(
            message = chats,
            status = HttpStatusCode.OK
        )
    }
}

fun Route.getChat(chatDataSource: ChatDataSource) {
    get("chat") {
        val chatId = call.parameters["chatId"] ?: kotlin.run {
            call.respond(
                message = "Chat ID is invalid.",
                status = HttpStatusCode.Conflict
            )
            return@get
        }
        val chat = chatDataSource.getChatById(chatId) ?: kotlin.run {
            call.respond(
                message = "This chat has not yet been created",
                status = HttpStatusCode.Conflict
            )
            return@get
        }
        call.respond(
            message = chat,
            status = HttpStatusCode.OK
        )
    }
}