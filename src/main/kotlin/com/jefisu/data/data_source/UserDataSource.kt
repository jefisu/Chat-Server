package com.jefisu.data.data_source

import com.jefisu.data.model.User
import com.jefisu.data.model.response.UserDto

interface UserDataSource {

    suspend fun getUserByLogin(login: String): User?

    suspend fun insertUser(user: User): Boolean

    suspend fun getAllUsers(ownerUsername: String): List<User>

    suspend fun changeAvatar(userId: String, avatarUrl: String): Boolean

    suspend fun updateUserInfo(user: UserDto): Boolean

    suspend fun changePassword(user: User, password: String): Boolean
}