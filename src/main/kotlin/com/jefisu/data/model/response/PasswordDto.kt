package com.jefisu.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PasswordDto(
    val username: String,
    val oldPassword: String,
    val newPassword: String
)
