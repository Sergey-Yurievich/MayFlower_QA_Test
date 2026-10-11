package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.assertions.assertThatError
import com.example.apitests.config.UserFields
import com.example.apitests.data.UserFactory
import com.example.apitests.models.ErrorMessages
import com.example.apitests.models.ErrorResponse
import com.example.apitests.models.parseAs
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Уникальность `username` и `email`.
 */
@Epic("User API")
@Feature("POST /user/create — уникальность")
@DisplayName("Регистрация: уникальность username и email")
class UserCreateUniquenessTest {

    private val userApi = UserApi()

    @Test
    @AllureId("TC-U01")
    @Tag("Regress")
    @Story("Уникальность username")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с занятым username")
    fun `duplicate username is rejected`() {
        val first = UserFactory.validUser("dup")
        userApi.createUserSuccessfullyAndGetData(first)

        val response = userApi.createUser(UserFactory.withUsername(first.username))

        assertThat(response.statusCode).describedAs("Регистрация с занятым username не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.USERNAME_TAKEN)
    }

    @Test
    @AllureId("TC-U02")
    @Tag("Regress")
    @Story("Уникальность email")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с занятым email")
    fun `duplicate email is rejected`() {
        val first = UserFactory.validUser("dup")
        userApi.createUserSuccessfullyAndGetData(first)

        val response = userApi.createUser(UserFactory.withEmail(first.email))

        assertThat(response.statusCode).describedAs("Регистрация с занятым email не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.EMAIL_EXISTS)
    }

    @Test
    @AllureId("TC-U03")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("Уникальность username")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Отклонённый дубль не создаёт вторую запись")
    fun `rejected duplicate does not create second record`() {
        val first = UserFactory.validUser("dup")
        userApi.createUserSuccessfullyAndGetData(first)

        userApi.createUser(UserFactory.withUsername(first.username))

        assertThat(userApi.findUsersBy(UserFields.USERNAME, first.username!!))
            .describedAs("Отклонённый дубль создал вторую запись с тем же username")
            .hasSize(1)
    }

    @Test
    @AllureId("TC-U08")
    @Tag("Regress")
    @Story("Уникальность username")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Повторная отправка той же формы не создаёт второго пользователя")
    fun `resubmitted form does not create second user`() {
        val request = UserFactory.validUser("dup")
        userApi.createUserSuccessfullyAndGetData(request)

        val response = userApi.createUser(request)

        assertThat(response.statusCode).describedAs("Повторная отправка той же формы не отклонена с 400").isEqualTo(400)
        assertThat(userApi.findUsersBy(UserFields.USERNAME, request.username!!))
            .describedAs("Повторная отправка той же формы создала второго пользователя")
            .hasSize(1)
    }
}
