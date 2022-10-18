package com.jefisu.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Chat(
    val userIds: List<String>,
    val messages: List<Message>,
    val createdAt: Long,
    @BsonId val id: String = ObjectId().toString()
)