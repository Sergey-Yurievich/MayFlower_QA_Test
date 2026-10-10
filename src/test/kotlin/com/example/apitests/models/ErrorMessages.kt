package com.example.apitests.models

/**
 * Тексты ошибок валидации, зафиксированные по результатам исследования API
 */
object ErrorMessages {
    const val USERNAME_REQUIRED: String = "A username is required"
    const val EMAIL_REQUIRED: String = "An Email is required"
    const val PASSWORD_REQUIRED: String = "A password for the user"
    const val USERNAME_TAKEN: String = "This username is taken. Try another."
    const val EMAIL_EXISTS: String = "Email already exists"
}
