package com.jefisu.data.database

import com.jefisu.data.data_source.UserDataSource
import com.jefisu.data.model.User
import com.jefisu.data.model.response.UserDto
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.ne

class MongoUserDataSource(
    db: CoroutineDatabase
) : UserDataSource {

    private val users = db.getCollection<User>()

    override suspend fun getUserById(id: String): User? {
        return users.findOneById(id)
    }

    override suspend fun getUserByLogin(login: String): User? {
        val user = users.findOne(User::username eq login)
        user?.let { return it }

        return users.findOne(User::email eq login)
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getAllUsers(ownerUsername: String): List<User> {
        return users.find(User::username ne ownerUsername).toList()
    }

    override suspend fun changeAvatar(userId: String, avatarUrl: String): Boolean {
        val user = users.findOneById(userId)
        if (user != null) {
            return users.updateOneById(
                id = userId,
                update = user.copy(avatarUrl = avatarUrl)
            ).wasAcknowledged()
        }
        return false
    }

    override suspend fun updateUserInfo(user: UserDto): Boolean {
        val currentUser = users.findOneById(user.id)
        if (currentUser != null) {
            return users.updateOneById(
                id = currentUser.id,
                update = currentUser.copy(
                    name = user.name,
                    username = user.username,
                    email = user.email
                )
            ).wasAcknowledged()
        }
        return false
    }

    override suspend fun changePassword(user: User, password: String): Boolean {
        return users.updateOneById(
            id = user.id,
            update = user.copy(
                password = password
            )
        ).wasAcknowledged()
    }
}