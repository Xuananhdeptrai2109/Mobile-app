package com.moneycare.app.data.model.auth

data class RegisterRequest(
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val password: String,
    val confirmPassword: String
)
