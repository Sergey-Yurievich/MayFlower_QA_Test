package com.example.apitests.api

import com.example.apitests.config.Endpoints
import com.example.apitests.config.UserFields
import com.example.apitests.config.TestConfig
import com.example.apitests.models.CreateUserRequest
import com.example.apitests.models.CreateUserSuccessResponse
import com.example.apitests.models.UserDto
import com.example.apitests.models.parseAs
import io.qameta.allure.Step
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.specification.RequestSpecification
import java.nio.charset.StandardCharsets

/**
 * API-объект пользователя
 *
 * Инкапсулирует всё, что тест знать не должен: пути, способ сериализации полей
 * (`multipart/form-data`), коды ответов, разбор JSON.
 */
class UserApi(baseUrl: String = TestConfig.baseUrl) : BaseApi(baseUrl) {

    @Step("Вызов метода `POST /user/create` с телом multipart/form-data")
    fun createUser(request: CreateUserRequest): Response = request()
        .multiPartIfPresent(UserFields.USERNAME, request.username)
        .multiPartIfPresent(UserFields.EMAIL, request.email)
        .multiPartIfPresent(UserFields.PASSWORD, request.password)
        .request(Method.POST, Endpoints.USER_CREATE)

    @Step("Вызов метода `POST /user/create` с заданным набором полей формы")
    fun createUserWithFormFields(fields: List<Pair<String, String>>): Response =
        fields.fold(request()) { spec, (name, value) -> spec.multiPart(name, value) }
            .request(Method.POST, Endpoints.USER_CREATE)

    @Step("Вызов метода `POST /user/create` с дополнительными полями формы")
    fun createUserWithExtraFields(request: CreateUserRequest, extraFields: Map<String, String>): Response =
        extraFields.entries
            .fold(
                request()
                    .multiPartIfPresent(UserFields.USERNAME, request.username)
                    .multiPartIfPresent(UserFields.EMAIL, request.email)
                    .multiPartIfPresent(UserFields.PASSWORD, request.password)
            ) { spec, (name, value) -> spec.multiPartIfPresent(name, value) }
            .request(Method.POST, Endpoints.USER_CREATE)

    @Step("Вызов метода `POST /user/create` с телом без сериализации")
    fun createUserWithCustomBody(contentType: String, body: String): Response = request()
        .contentType(contentType)
        .body(body)
        .request(Method.POST, Endpoints.USER_CREATE)

    @Step("Вызов метода `GET /user/get`")
    fun getUsers(): Response = request().request(Method.GET, Endpoints.USER_GET)

    @Step("Вызов метода `GET /user/get` с query-параметрами")
    fun getUsersWithQueryParams(params: Map<String, String>): Response = request()
        .queryParams(params)
        .request(Method.GET, Endpoints.USER_GET)

    @Step("Регистрация пользователя и получение его данных")
    fun createUserSuccessfullyAndGetData(request: CreateUserRequest): UserDto {
        val response = createUser(request)
        require(response.statusCode == 200) {
            "Ожидался успешный 200 на создание пользователя, получено ${response.statusCode}: ${response.asString()}"
        }
        return response.parseAs<CreateUserSuccessResponse>().details
            ?: error("В успешном ответе отсутствует блок details: ${response.asString()}")
    }

    @Step("Получение списка всех пользователей")
    fun getUsersAsList(): List<UserDto> = getUsers().parseAs()

    @Step("Поиск пользователей по значению поля")
    fun findUsersBy(field: String, value: String): List<UserDto> =
        getUsersWithQueryParams(mapOf(field to value)).parseAs()

    /**
     * Добавляет текстовое multipart-поле в кодировке UTF-8.
     *
     * Короткая форма `multiPart(name, value)` отправляет часть в ISO-8859-1,
     * и кириллица превращается в «?». `emptyFileName()` обязателен: с именем
     * файла сервер посчитал бы часть загружаемым файлом, а не полем формы.
     */
    private fun RequestSpecification.multiPartIfPresent(name: String, value: String?): RequestSpecification =
        if (value == null) {
            this
        } else {
            multiPart(
                MultiPartSpecBuilder(value)
                    .controlName(name)
                    .charset(StandardCharsets.UTF_8)
                    .mimeType("text/plain")
                    .emptyFileName()
                    .build()
            )
        }
}
