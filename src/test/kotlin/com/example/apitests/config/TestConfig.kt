package com.example.apitests.config

/**
 * Единая точка конфигурации тестов.
 *
 * Значения приходят из системных свойств, которые проставляет Gradle, поэтому один и тот же набор тестов
 * запускается и против реального sandbox-а, и против локального WireMock:
 *     ./gradlew regressTest
 *     ./gradlew regressTest -Papi.baseUrl=http://localhost:8080
 */
object TestConfig {

    /** Sandbox из условия задания. */
    const val SANDBOX_BASE_URL: String = "http://18.194.45.232:3333"

    val baseUrl: String = System.getProperty("api.baseUrl")?.takeIf { it.isNotBlank() } ?: SANDBOX_BASE_URL

    /** Таймаут соединения и чтения для HTTP-клиента, мс. */
    const val CONNECT_TIMEOUT_MS: Int = 10_000
    const val SOCKET_TIMEOUT_MS: Int = 15_000
}
