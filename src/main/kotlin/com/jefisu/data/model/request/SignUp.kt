package com.jefisu.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SignUp(
    val username: String,
    val email: String,
    val password: String
)