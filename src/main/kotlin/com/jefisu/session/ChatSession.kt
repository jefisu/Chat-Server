package com.jefisu.session

import com.jefisu.data.model.Chat
import com.jefisu.data.model.User

data class ChatSession(
    val ownerId: String,
    val recipientId: String,
    val chat: Chat
)