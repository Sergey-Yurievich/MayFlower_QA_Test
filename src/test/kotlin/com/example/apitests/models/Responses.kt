package com.example.apitests.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Успешный ответ `POST /user/create`..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserSuccessResponse(
    val success: Boolean = false,
    val details: UserDto? = null,
    val message: String? = null,
)

/** Ответ с ошибкой валидации: `message` — массив строк. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorResponse(
    val success: Boolean = true,
    val message: List<String> = emptyList(),
)
