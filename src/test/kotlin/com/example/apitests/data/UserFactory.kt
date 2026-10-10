package com.example.apitests.data

import com.example.apitests.models.CreateUserRequest
import java.util.UUID

/**
 * Генератор тестовых данных.
 *
 * У API нет эндпоинта удаления. Единственный способ сделать прогон повторяемым — никогда не
 * переиспользовать `username` и `email`. Отсюда UUID-суффикс в каждом значении.
 */
object UserFactory {

    private const val VALID_PASSWORD = "Passw0rd!2026"

    /** Короткий уникальный суффикс: 10 hex-символов, этого достаточно против коллизий. */
    fun suffix(): String = UUID.randomUUID().toString().replace("-", "").take(10)

    fun uniqueUsername(prefix: String = "qa"): String = "${prefix}_${suffix()}"

    fun uniqueEmail(prefix: String = "qa"): String = "${prefix}_${suffix()}@example.test"

    /** Полностью валидный запрос на регистрацию с уникальными данными. */
    fun validUser(prefix: String = "qa"): CreateUserRequest = CreateUserRequest(
        username = uniqueUsername(prefix),
        email = uniqueEmail(prefix),
        password = VALID_PASSWORD,
    )

    /** Валидный запрос с заданным username, остальное — уникальное. */
    fun withUsername(username: String?): CreateUserRequest =
        validUser().copy(username = username)

    /** Валидный запрос с заданным email, остальное — уникальное. */
    fun withEmail(email: String?): CreateUserRequest =
        validUser().copy(email = email)

    /** Валидный запрос с заданным password, остальное — уникальное. */
    fun withPassword(password: String?): CreateUserRequest =
        validUser().copy(password = password)
}

/** Наборы данных для негативных проверок. */
object TestData {

    /** Классические SQL-инъекции. Значения должны параметризоваться, а не исполняться. */
    object SqlInjection {
        const val DROP_TABLE: String = "robert'); DROP TABLE users;--"
        const val ALWAYS_TRUE: String = "' OR '1'='1"
    }
}
