package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.data.UserFactory
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * HTTP-уровень: Content-Type ответов.
 */
@Epic("User API")
@Feature("HTTP-контракт")
@DisplayName("HTTP-контракт: Content-Type ответов")
class HttpContractTest {

    private val userApi = UserApi()

    // Content-Type успешного ответа GET /user/get проверяет TC-G01.
    @Test
    @AllureId("TC-H13")
    @Tag("Regress")
    @Story("Content-Type ответа")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Эндпоинт /user/create. Успешный ответ приходит как application/json")
    fun `successful create response is json`() {
        val response = userApi.createUser(UserFactory.validUser("ct"))

        assertThat(ContentType.fromContentType(response.contentType))
            .describedAs("POST /user/create ответил не JSON")
            .isEqualTo(ContentType.JSON)
    }

    @Test
    @AllureId("TC-H14")
    @Tag("Regress")
    @Story("Ответы с ошибкой")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Эндпоинт /user/create. Ошибка валидации приходит как application/json")
    fun `validation error is json`() {
        val response = userApi.createUser(UserFactory.withUsername(null))

        assertThat(response.statusCode).describedAs("Регистрация без username не отклонена с 400").isEqualTo(400)
        assertThat(ContentType.fromContentType(response.contentType))
            .describedAs("Ошибка валидации пришла не в JSON")
            .isEqualTo(ContentType.JSON)
    }
}
