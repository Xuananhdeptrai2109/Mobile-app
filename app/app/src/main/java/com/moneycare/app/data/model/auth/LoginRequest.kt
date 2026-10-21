package com.moneycare.app.data.model.auth

data class LoginRequest(
    val emailOrPhoneNumber: String,
    val password: String
)
