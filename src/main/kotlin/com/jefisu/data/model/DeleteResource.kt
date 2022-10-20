package com.jefisu.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeleteResource(
    val id: String,
    val items: List<String>
)