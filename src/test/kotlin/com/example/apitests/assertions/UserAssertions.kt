package com.example.apitests.assertions

import com.example.apitests.models.CreateUserRequest
import com.example.apitests.models.ErrorResponse
import com.example.apitests.models.UserDto
import io.qameta.allure.Step
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

/**
 * Доменные ассерты поверх AssertJ
 */
class UserAssert(actual: UserDto?) : AbstractAssert<UserAssert, UserDto?>(actual, UserAssert::class.java) {

    @Step("Проверка: пользователю присвоен id")
    fun hasAssignedId(): UserAssert {
        assertThat(user().id)
            .describedAs(describe("У юзера отсутствует id"))
            .isNotNull()
            .isGreaterThan(0L)
        return this
    }

    @Step("Проверка: username совпадает с ожидаемым")
    fun hasUsername(expected: String): UserAssert {
        assertThat(user().username).describedAs(describe("username не совпадает с ожидаемым")).isEqualTo(expected)
        return this
    }

    @Step("Проверка: email совпадает с ожидаемым")
    fun hasEmail(expected: String): UserAssert {
        assertThat(user().email).describedAs(describe("email не совпадает с ожидаемым")).isEqualTo(expected)
        return this
    }

    @Step("Проверка: username и email совпадают с отправленными в запросе")
    fun hasUsernameAndEmailFrom(request: CreateUserRequest): UserAssert {
        hasUsername(request.username!!.trim())
        hasEmail(request.email!!)
        return this
    }

    @Step("Проверка: created_at и updated_at заполнены")
    fun hasCreatedAtAndUpdatedAt(): UserAssert {
        assertThat(user().createdAt).describedAs(describe("Не заполнен created_at")).isNotBlank()
        assertThat(user().updatedAt).describedAs(describe("Не заполнен updated_at")).isNotBlank()
        return this
    }

    @Step("Проверка: пароль не возвращается в ответе")
    fun doesNotExposePassword(): UserAssert {
        assertThat(user().password)
            .describedAs(describe("Ответ возвращает пароль юзера"))
            .isNull()
        return this
    }

    private fun user(): UserDto {
        isNotNull
        return actual!!
    }

    /** Добавляет к тексту ошибки описание, заданное в тесте через `describedAs`. */
    private fun describe(what: String): String =
        info.descriptionText().takeIf { it.isNotEmpty() }?.let { "$it: $what" } ?: what
}

class ErrorResponseAssert(actual: ErrorResponse?) :
    AbstractAssert<ErrorResponseAssert, ErrorResponse?>(actual, ErrorResponseAssert::class.java) {

    @Step("Проверка: в ответе success=false")
    fun hasSuccessFalse(): ErrorResponseAssert {
        assertThat(error().success).describedAs("В ответе с ошибкой success не равен false").isFalse()
        return this
    }

    @Step("Проверка: в ответе есть ожидаемое сообщение об ошибке")
    fun containsMessage(expected: String): ErrorResponseAssert {
        assertThat(error().message)
            .describedAs("Описание ошибки не соответствует требованиям")
            .contains(expected)
        return this
    }

    @Step("Проверка: в ответе есть сообщение об ошибке")
    fun hasAtLeastOneMessage(): ErrorResponseAssert {
        assertThat(error().message)
            .describedAs("В ответе нет сообщения об ошибке")
            .isNotEmpty()
        return this
    }

    private fun error(): ErrorResponse {
        isNotNull
        return actual!!
    }
}

fun assertThatUser(actual: UserDto?): UserAssert = UserAssert(actual)

fun assertThatError(actual: ErrorResponse?): ErrorResponseAssert = ErrorResponseAssert(actual)
