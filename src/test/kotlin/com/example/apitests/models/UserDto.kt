package com.example.apitests.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Пользователь в том виде, в котором его отдаёт API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDto(
    val id: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    @JsonProperty("created_at") val createdAt: String? = null,
    @JsonProperty("updated_at") val updatedAt: String? = null,
)
