package com.example.apitests.specs

import com.example.apitests.config.TestConfig
import io.qameta.allure.restassured.AllureRestAssured
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.EncoderConfig.encoderConfig
import io.restassured.config.HttpClientConfig
import io.restassured.config.MultiPartConfig.multiPartConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import java.nio.charset.StandardCharsets

/**
 * Request-спецификация REST Assured.
 */
object Specs {

    init {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    private val httpConfig: RestAssuredConfig = RestAssured.config()
        .httpClient(
            HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", TestConfig.CONNECT_TIMEOUT_MS)
                .setParam("http.socket.timeout", TestConfig.SOCKET_TIMEOUT_MS)
        )
        // Без явной кодировки REST Assured отправляет части multipart в ISO-8859-1,
        // и кириллица с умляутами превращаются в «?». Тест должен падать на дефектах
        // API, а не на настройках собственного клиента.
        .encoderConfig(
            encoderConfig()
                .defaultContentCharset(StandardCharsets.UTF_8)
                .defaultCharsetForContentType(StandardCharsets.UTF_8.name(), ContentType.TEXT)
                .defaultCharsetForContentType(StandardCharsets.UTF_8.name(), "multipart/form-data")
        )
        .multiPartConfig(multiPartConfig().defaultCharset(StandardCharsets.UTF_8))

    fun requestSpec(baseUrl: String): RequestSpecification = RequestSpecBuilder()
        .setBaseUri(baseUrl)
        .setConfig(httpConfig)
        .addFilter(AllureRestAssured())
        .build()
}
