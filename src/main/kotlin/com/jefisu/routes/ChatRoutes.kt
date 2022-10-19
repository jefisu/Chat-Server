package com.jefisu.routes

import com.jefisu.data.chat.ChatController
import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.session.ChatSession
import io.ktor.http.*
import io.ktor.server.application.*
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
            session.ownerId,
            session.chat.id,
            this
        )
        if (!joinChat) {
            webSocketRaw {
                chatController.joinChat(
                    session.ownerId,
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
                        userId = session.ownerId,
                        text = frame.readText()
                    )
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            e.printStackTrace()
        } finally {
            chatController.exitChat(session.ownerId)
        }
    }
}

fun Route.chatsByUser(chatDataSource: ChatDataSource) {
    post("chats") {
        val userId = call.parameters["userId"] ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "User not registered"
            )
            return@post
        }
        val chats = chatDataSource.getChatsByUser(userId)
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

fun Route.deleteChat(chatDataSource: ChatDataSource) {
    delete ("chat/bin") {
        val chatId = call.parameters["chatId"] ?: kotlin.run {
            call.respond(
                message = "Chat ID is invalid.",
                status = HttpStatusCode.Conflict
            )
            return@delete
        }
        val wasAcknowledged = chatDataSource.deleteChat(chatId)
        if (!wasAcknowledged) {
            call.respond(
                message = "Couldn't delete chat",
                status = HttpStatusCode.Conflict
            )
            return@delete
        }
        call.respond(
            message = "Chat successfully deleted",
            status = HttpStatusCode.OK
        )
    }
}