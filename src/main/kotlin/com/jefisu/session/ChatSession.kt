package com.jefisu.session

import com.jefisu.data.model.Chat
import com.jefisu.data.model.User

data class ChatSession(
    val ownerUser: User,
    val recipientUser: User,
    val chat: Chat
)