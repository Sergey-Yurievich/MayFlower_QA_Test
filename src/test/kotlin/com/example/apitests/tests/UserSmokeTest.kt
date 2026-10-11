package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.assertions.assertThatUser
import com.example.apitests.config.UserFields
import com.example.apitests.data.UserFactory
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * P0-смоук основного флоу: если эти три теста красные, остальной прогон смысла
 * не имеет. Тег `Smoke` стоит у каждого P0-теста в любом классе, не только здесь.
 */
@Epic("User API")
@Feature("Смоук основного флоу")
@DisplayName("Смоук: регистрация и список пользователей")
class UserSmokeTest {

    private val userApi = UserApi()

    @Test
    @AllureId("TC-S01")
    @Tag("Smoke")
    @Tag("Regress")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/create. Пользователь с валидными данными регистрируется")
    fun `valid user is registered`() {
        val request = UserFactory.validUser("smoke")

        val response = userApi.createUser(request)

        assertThat(response.statusCode).describedAs("Регистрация с валидными данными вернула не 200").isEqualTo(200)
        assertThat(response.jsonPath().getBoolean("success"))
            .describedAs("Успешная регистрация вернула success != true")
            .isTrue()
        assertThat(response.jsonPath().getLong("details.id"))
            .describedAs("Созданному пользователю не присвоен id")
            .isGreaterThan(0L)
        assertThat(response.jsonPath().getString("details.username"))
            .describedAs("В ответе на регистрацию username не совпадает с отправленным")
            .isEqualTo(request.username)
    }

    @Test
    @AllureId("TC-S02")
    @Tag("Smoke")
    @Tag("Regress")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. Эндпоинт доступен и отвечает JSON")
    fun `user list endpoint is available`() {
        val response = userApi.getUsers()

        assertThat(response.statusCode).describedAs("GET /user/get вернул не 200").isEqualTo(200)
        assertThat(response.contentType).describedAs("GET /user/get ответил не JSON").contains("application/json")
    }

    @Test
    @AllureId("TC-S03")
    @Tag("Smoke")
    @Tag("Regress")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. Зарегистрированный пользователь находится по username")
    fun `registered user is retrievable`() {
        val request = UserFactory.validUser("smoke")

        val created = userApi.createUserSuccessfullyAndGetData(request)

        val found = userApi.findUsersBy(UserFields.USERNAME, request.username!!)
        assertThat(found).describedAs("Зарегистрированный пользователь не найден по username или найден не один").hasSize(1)
        assertThatUser(found.single())
            .hasAssignedId()
            .hasUsername(request.username)
            .hasEmail(request.email!!)
        assertThat(found.single().id).describedAs("По username найден не тот пользователь, что был создан").isEqualTo(created.id)
    }
}
