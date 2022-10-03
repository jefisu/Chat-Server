package com.jefisu.data.model

import com.jefisu.data.model.response.UserDto
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Chat(
    val users: List<UserDto>,
    val messages: List<Message>,
    val createdAt: Long,
    @BsonId val id: String = ObjectId().toString()
)