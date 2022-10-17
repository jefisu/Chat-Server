package com.jefisu.data.chat

import com.jefisu.data.data_source.ChatDataSource
import com.jefisu.data.model.Chat
import com.jefisu.data.model.Message
import com.jefisu.data.model.User
import com.jefisu.session.Member
import io.ktor.util.collections.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChatController(
    private val chatDataSource: ChatDataSource
) {
    private val members = ConcurrentMap<String, Member>()

    fun joinChat(
        ownerUsername: String,
        chatId: String,
        socket: WebSocketSession
    ): Boolean {
        val currentSocketWithUsers = members.values.count { it.socket == socket }
        val userHasExistChat = members.any { it.key == ownerUsername }
        if (currentSocketWithUsers <= 1 || userHasExistChat) {
            members[ownerUsername] = Member(chatId, socket)
            return true
        }
        return false
    }

    suspend fun sendMessage(
        chat: Chat,
        ownerUser: User,
        text: String
    ) {
        val message = Message(
            text = text,
            userId = ownerUser.id,
            timestamp = System.currentTimeMillis()
        )
        chatDataSource.insertMessage(chat, message)

        val parsedMessage = Json.encodeToString(message)
        members.values
            .filter { it.chatId == chat.id }
            .forEach { it.socket.send(parsedMessage) }
    }

    suspend fun exitChat(username: String) {
        members.apply {
            get(username)?.socket?.close()
            remove(username)
        }
    }
}
