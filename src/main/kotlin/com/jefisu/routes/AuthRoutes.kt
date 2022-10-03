package com.jefisu.routes

import com.jefisu.data.data_source.UserDataSource
import com.jefisu.data.mapper.toUserDto
import com.jefisu.data.model.User
import com.jefisu.data.model.request.SignIn
import com.jefisu.data.model.request.SignUp
import com.jefisu.security.hash.HashService
import com.jefisu.security.hash.SaltedHash
import com.jefisu.security.token.TokenClaim
import com.jefisu.security.token.TokenConfig
import com.jefisu.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.auth(userDataSource: UserDataSource) {
    authenticate {
        get("auth") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.getClaim("username", String::class) ?: return@get
            val user = userDataSource.getUserByLogin(username) ?: kotlin.run {
                call.respond(
                    message = "User not found",
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
}

fun Route.signUp(
    userDataSource: UserDataSource,
    hashService: HashService
) {
    post("signup") {
        val request = call.receiveNullable<SignUp>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areBlankFields = request.email.isBlank()
                || request.username.isBlank()
                || request.password.isBlank()
        if (areBlankFields) {
            call.respond(
                message = "Fill in all fields.",
                status = HttpStatusCode.Conflict
            )
            return@post
        }

        val isPasswordTooShort = request.password.length < 8
        if (isPasswordTooShort) {
            call.respond(
                message = "Is password too short.",
                status = HttpStatusCode.Conflict
            )
            return@post
        }

        userDataSource.apply {
            getUserByLogin(request.username)?.let {
                call.respond(
                    message = "A user with that username already exists.",
                    status = HttpStatusCode.Conflict
                )
                return@post
            }
            getUserByLogin(request.email)?.let {
                call.respond(
                    message = "There is already a user with this registered email.",
                    status = HttpStatusCode.Conflict
                )
                return@post
            }
        }

        val saltedHash = hashService.generate(request.password)
        val user = User(
            username = request.username,
            email = request.email,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            avatarUrl = null,
            name = null
        )
        val userIsCreated = userDataSource.insertUser(user)
        if (!userIsCreated) {
            call.respond(
                message = "Unexpected error.",
                status = HttpStatusCode.Conflict
            )
            return@post
        }

        call.respond(
            message = "Created user with success.",
            status = HttpStatusCode.OK
        )
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashService: HashService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveNullable<SignIn>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByLogin(request.login) ?: kotlin.run {
            call.respond(
                message = "Incorrect username or email.",
                status = HttpStatusCode.Conflict
            )
            return@post
        }

        val validPassword = hashService.verify(
            value = request.password,
            saltedHash = SaltedHash(user.password, user.salt)
        )
        if (!validPassword) {
            call.respond(
                message = "Incorrect password.",
                status = HttpStatusCode.Conflict
            )
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            claim = TokenClaim(
                name = "username",
                value = user.username
            )
        )
        call.respond(
            message = token,
            status = HttpStatusCode.OK
        )
    }
}