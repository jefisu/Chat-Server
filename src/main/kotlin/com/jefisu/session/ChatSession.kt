package com.jefisu.session

import com.jefisu.data.model.Chat

data class ChatSession(
    val ownerId: String,
    val recipientId: String,
    val chat: Chat
)