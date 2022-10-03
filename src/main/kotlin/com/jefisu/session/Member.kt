package com.jefisu.session

import io.ktor.websocket.*

data class Member(
    val chatId: String,
    val socket: WebSocketSession
)