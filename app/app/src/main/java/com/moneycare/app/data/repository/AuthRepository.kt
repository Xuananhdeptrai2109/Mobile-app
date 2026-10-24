package com.moneycare.app.data.repository

import com.google.gson.Gson
import com.moneycare.app.data.api.ApiService
import com.moneycare.app.data.model.auth.AuthResponse
import com.moneycare.app.data.model.auth.LoginRequest
import com.moneycare.app.data.model.auth.RegisterRequest
import com.moneycare.app.util.SessionManager
import retrofit2.HttpException

/**
 * Owns the authentication data flow. ViewModels validate input and render state;
 * they do not know how Retrofit or session persistence works.
 */
class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(request: LoginRequest): AuthResponse =
        persistSuccessfulSession(apiCall = { apiService.login(request) })

    suspend fun register(request: RegisterRequest): AuthResponse =
        persistSuccessfulSession(apiCall = { apiService.register(request) })

    private suspend fun persistSuccessfulSession(
        apiCall: suspend () -> AuthResponse
    ): AuthResponse {
        val response = try {
            apiCall()
        } catch (error: HttpException) {
            throw AuthRepositoryException(error.apiMessage(), error)
        }

        val token = response.token
        val userId = response.userId
        if (!response.success || token.isNullOrBlank() || userId == null) {
            throw AuthRepositoryException(response.message ?: "Phản hồi xác thực không hợp lệ")
        }

        sessionManager.saveSession(
            token = token,
            userId = userId,
            userName = response.fullName.orEmpty()
        )
        return response
    }
}

class AuthRepositoryException(
    override val message: String,
    cause: Throwable? = null
) : Exception(message, cause)

private fun HttpException.apiMessage(): String {
    val fallback = if (code() == 401) {
        "Thông tin đăng nhập không chính xác"
    } else {
        "Máy chủ từ chối yêu cầu (${code()})"
    }
    return runCatching {
        val body = response()?.errorBody()?.string() ?: return@runCatching fallback
        val values: Map<*, *> = Gson().fromJson(body, Map::class.java)
        values["message"]?.toString()?.takeIf(String::isNotBlank) ?: fallback
    }.getOrDefault(fallback)
}
