package com.moneycare.app.data.model.auth

data class AuthResponse(
    val userId: Long?,
    val fullName: String?,
    val email: String?,
    val phoneNumber: String?,
    val token: String?,
    val message: String,
    val success: Boolean
)
