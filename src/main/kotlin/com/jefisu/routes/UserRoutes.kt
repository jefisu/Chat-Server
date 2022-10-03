package com.jefisu.routes

import com.jefisu.data.data_source.UserDataSource
import com.jefisu.data.mapper.toUserDto
import com.jefisu.data.model.response.PasswordDto
import com.jefisu.data.model.response.UserDto
import com.jefisu.data.util.save
import com.jefisu.security.hash.HashService
import com.jefisu.security.hash.SaltedHash
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*


fun Route.getUser(userDataSource: UserDataSource) {
    get("user") {
        val username = call.parameters["username"] ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "Invalid user"
            )
            return@get
        }
        val user = userDataSource.getUserByLogin(username) ?: kotlin.run {
            call.respond(
                message = "User not found.",
                status = HttpStatusCode.Conflict
            )
            return@get
        }
        call.respond(
            message = user.toUserDto(),
            status = HttpStatusCode.OK
        )
    }
}

fun Route.getAllUsers(userDataSource: UserDataSource) {
    get("users") {
        val ownerUsername = call.parameters["ownerUsername"] ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "Invalid user"
            )
            return@get
        }
        val users = userDataSource.getAllUsers(ownerUsername)
        call.respond(
            status = HttpStatusCode.OK,
            message = users.map { it.toUserDto() }
        )
    }
}

fun Route.changeAvatar(userDataSource: UserDataSource) {
    put("user/change-avatar") {
        val userId = call.parameters["userId"] ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "UserId invalid."
            )
            return@put
        }
        val multipart = call.receiveMultipart()
        multipart.forEachPart { partData ->
            if (partData is PartData.FileItem) {
                val file = partData.save("build/resources/main/static/images/$userId/", "${generateNonce()}.png")
                val wasAcknowledged =
                    userDataSource.changeAvatar(userId, "http://192.168.0.2:8080/images/$userId/${file.name}")
                if (!wasAcknowledged) {
                    call.respond(
                        status = HttpStatusCode.Conflict,
                        message = "Unable to change avatar"
                    )
                    return@forEachPart
                }
                call.respond(
                    status = HttpStatusCode.OK,
                    message = "Avatar successfully updated."
                )
            }
        }
    }
}

fun Route.changeUserInfo(userDataSource: UserDataSource) {
    put("user/change-info") {
        val user = call.receiveNullable<UserDto>() ?: kotlin.run {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "Unable to update data"
            )
            return@put
        }
        val wasAcknowledged = userDataSource.updateUserInfo(user)
        if (!wasAcknowledged) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = "Unable to update data"
            )
            return@put
        }
        call.respond(
            message = "Data successfully updated.",
            status = HttpStatusCode.OK
        )
    }
}

fun Route.changePassword(
    userDataSource: UserDataSource,
    hashService: HashService
) {
    put("user/change-password") {
        val request = call.receiveNullable<PasswordDto>() ?: kotlin.run {
            call.respond(
                message = "Couldn't change password",
                status = HttpStatusCode.Conflict
            )
            return@put
        }
        val user = userDataSource.getUserByLogin(request.username) ?: kotlin.run {
            call.respond(
                message = "This user does not exist in the registry",
                status = HttpStatusCode.Conflict
            )
            return@put
        }
        val checkPassword = hashService.verify(
            value = request.oldPassword,
            saltedHash = SaltedHash(user.password, user.salt)
        )
        if (!checkPassword) {
            call.respond(
                message = "This is not the old password",
                status = HttpStatusCode.Conflict
            )
            return@put
        }

        val saltedHash = hashService.generate(request.newPassword)
        val wasAcknowledged = userDataSource.changePassword(user.copy(salt = saltedHash.salt), saltedHash.hash)
        if (!wasAcknowledged) {
            call.respond(
                message = "Unexpected error",
                status = HttpStatusCode.Conflict
            )
            return@put
        }
        call.respond(
            message = "Password successfully updated.",
            status = HttpStatusCode.OK
        )
    }
}