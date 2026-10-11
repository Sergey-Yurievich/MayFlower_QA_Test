package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.assertions.assertThatUser
import com.example.apitests.models.UserDto
import com.example.apitests.models.parseAs
import com.example.apitests.wiremock.UserStubs
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Контрактные тесты на WireMock.
 *
 * Проверяют форму ответа (схема, типы, набор полей) на фиксированных данных:
 * на общей базе sandbox-а это проверить нельзя.
 */
@Epic("User API")
@Feature("Контракт запроса и ответа")
@Tag("contract")
@DisplayName("Контракт User API")
class UserApiContractTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock: WireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .failOnUnmatchedRequests(false)
            .build()
    }

    private lateinit var userApi: UserApi

    @BeforeEach
    fun setUp() {
        userApi = UserApi(wiremock.baseUrl())
    }

    @Test
    @AllureId("TC-C04")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("Список пользователей")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. Список соответствует корректной схеме без поля password")
    fun `user list matches expected schema`() {
        wiremock.stubFor(UserStubs.correctUserList())

        val response = userApi.getUsers()

        response.then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/users-list-expected.json"))

        val users = response.parseAs<List<UserDto>>()
        assertThat(users).describedAs("Из списка на 2 пользователя разобрано другое количество").hasSize(2)
        users.forEach { assertThatUser(it).hasAssignedId().hasCreatedAtAndUpdatedAt().doesNotExposePassword() }
    }

    @Test
    @AllureId("TC-C05")
    @Tag("Regress")
    @Story("Список пользователей")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Эндпоинт /user/get. Пустая база возвращает [], а не ошибку")
    fun `empty user list is handled`() {
        wiremock.stubFor(UserStubs.emptyUserList())

        assertThat(userApi.getUsersAsList()).describedAs("Пустой список [] разобран не в пустую коллекцию").isEmpty()
    }
}
