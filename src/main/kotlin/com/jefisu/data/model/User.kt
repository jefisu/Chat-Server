package com.jefisu.data.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val name: String?,
    val username: String,
    val email: String,
    val password: String,
    val avatarUrl: String?,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis(),
    @BsonId val id: String = ObjectId().toString()
)