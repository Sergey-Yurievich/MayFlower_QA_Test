package com.example.apitests.models

data class CreateUserRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
)
