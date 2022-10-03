package com.jefisu.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SignIn(
    val login: String,
    val password: String
)