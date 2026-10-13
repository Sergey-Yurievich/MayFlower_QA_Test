package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.assertions.assertThatUser
import com.example.apitests.config.UserFields
import com.example.apitests.data.UserFactory
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Issue
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * `GET /user/get` — получение списка пользователей.
 */
@Epic("User API")
@Feature("GET /user/get")
@DisplayName("Список пользователей")
class UserGetTest {

    private val userApi = UserApi()

    @Test
    @AllureId("TC-G01")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("Базовый контракт")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. Возвращает 200 и Content-Type application/json")
    fun `returns 200 and json content type`() {
        val response = userApi.getUsers()

        assertThat(response.statusCode).describedAs("GET /user/get вернул не 200").isEqualTo(200)
        assertThat(response.contentType)
            .describedAs("GET /user/get ответил не JSON")
            .contains("application/json")
    }

    @Test
    @AllureId("TC-G02")
    @Tag("Regress")
    @Story("Базовый контракт")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/get. Ответ соответствует зафиксированной схеме")
    fun `matches actual schema`() {
        val request = UserFactory.validUser("get")
        userApi.createUserSuccessfullyAndGetData(request)

        // Схема users-list-actual.json описывает контракт, который API отдаёт
        // включая поле password. Её задача — поймать изменение формата.
        userApi.getUsersWithQueryParams(mapOf(UserFields.USERNAME to request.username!!))
            .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/users-list-actual.json"))
    }

    @Test
    @AllureId("TC-G03")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("Поиск своей записи")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. Созданный пользователь есть в списке со всеми полями")
    fun `created user is present with all fields`() {
        val request = UserFactory.validUser("get")
        val created = userApi.createUserSuccessfullyAndGetData(request)

        val found = userApi.findUsersBy(UserFields.USERNAME, request.username!!)

        assertThat(found).describedAs("Созданный пользователь не найден по username или найден не один").hasSize(1)
        assertThatUser(found.single())
            .hasAssignedId()
            .hasUsername(request.username)
            .hasEmail(request.email!!)
            .hasCreatedAtAndUpdatedAt()
        assertThat(found.single().id).describedAs("По username найден не тот пользователь, что был создан").isEqualTo(created.id)
    }

    @Test
    @AllureId("TC-G04")
    @Tag("Regress")
    @Story("Инварианты записи")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/get. Фильтр по id возвращает ровно одну запись")
    fun `id identifies single record`() {
        val created = userApi.createUserSuccessfullyAndGetData(UserFactory.validUser("get"))

        val byId = userApi.findUsersBy("id", created.id!!.toString())

        assertThat(byId)
            .describedAs("Фильтр по id вернул не ровно одну запись")
            .hasSize(1)
    }

    @Test
    @AllureId("TC-G05")
    @Tag("Regress")
    @Story("Инварианты записи")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Эндпоинт /user/get. У записи заполнены id, username, email, created_at и updated_at")
    fun `record has mandatory fields`() {
        val request = UserFactory.validUser("get")
        userApi.createUserSuccessfullyAndGetData(request)

        val user = userApi.findUsersBy(UserFields.USERNAME, request.username!!).single()

        assertThat(user.id).describedAs("У записи не заполнен id").isNotNull()
        assertThat(user.username).describedAs("У записи не заполнен username").isNotNull()
        assertThat(user.email).describedAs("У записи не заполнен email").isNotNull()
        assertThat(user.createdAt).describedAs("У записи не заполнен created_at").isNotBlank()
        assertThat(user.updatedAt).describedAs("У записи не заполнен updated_at").isNotBlank()
    }

    // Зависит от сети, откуда идёт прогон: из домашней и мобильной сети без VPN
    // тело крупнее ~12–34 КБ не доходит, и тест падает по таймауту (BUG-18).
    @Test
    @AllureId("TC-G07")
    @Tag("Smoke")
    @Tag("Regress")
    @Issue("BUG-18")
    @Story("Обрыв ответа")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. Тело ответа приходит целиком, по объявленному Content-Length")
    fun `user list delivers the body it declares`() {
        val response = userApi.getUsers()

        assertThat(response.asByteArray().size.toLong())
            .describedAs("Тело пришло не целиком: размер не совпадает с Content-Length")
            .isEqualTo(response.header("Content-Length")?.toLong())
    }

    // Тесты ниже описывают правильное поведение и падают на найденных дефектах.
    // Они пропущены через @Disabled с ID бага; исправят дефект — снять @Disabled.
    @Test
    @AllureId("TC-G17")
    @Tag("Regress")
    @Disabled("BUG-03: неизвестный query-параметр даёт 500 с текстом SQL")
    @Issue("BUG-03")
    @Story("Утечка данных")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/get. Неизвестный query-параметр не приводит к 500")
    fun `unknown query parameter must not leak sql`() {
        val response = userApi.getUsersWithQueryParams(mapOf("limit" to "1", "offset" to "0"))

        assertThat(response.statusCode)
            .describedAs("Неизвестный query-параметр привёл к 500")
            .isLessThan(500)
        assertThat(response.asString())
            .describedAs("В ответе на неизвестный query-параметр виден текст SQL-запроса")
            .doesNotContain("select * from")
            .doesNotContain("SQLITE_ERROR")
    }
}
