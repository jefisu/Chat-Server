package com.jefisu.data.mapper

import com.jefisu.data.model.User
import com.jefisu.data.model.response.UserDto

fun User.toUserDto(): UserDto {
    return UserDto(
        name = name,
        username = username,
        email = email,
        avatarUrl = avatarUrl,
        id = id
    )
}