package com.jefisu.data.data_source

import com.jefisu.data.model.Chat
import com.jefisu.data.model.Message
import com.jefisu.data.model.response.UserDto

interface ChatDataSource {
    suspend fun startNewChat(senderId: String, recipientId: String): Chat
    suspend fun insertMessage(chat: Chat, message: Message)
    suspend fun getChatById(chatId: String): Chat?
    suspend fun getChatsByUser(userId: String): List<Chat>
    suspend fun deleteChat(chatId: String)
}