package com.example.apitests.wiremock

import com.example.apitests.config.Endpoints
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo

/**
 * Стабы для контрактных тестов.
 *
 * Зачем они нужны при наличии живого sandbox-а:
 *
 *  1. База sandbox-а общая и меняется кем угодно — детерминированно проверить
 *     схему ответа и набор полей на ней невозможно, а пустую базу не получить.
 *  2. Стаб с *правильным* поведением (например, список без поля `password`)
 *     превращает найденный дефект в исполняемую спецификацию: на стабе тест
 *     зелёный, на реальном API — красный.
 */
object UserStubs {

    const val CORRECT_USERS_JSON: String = """
[
  {
    "id": 1,
    "username": "alice",
    "email": "alice@example.test",
    "created_at": "2026-10-09 10:00:00",
    "updated_at": "2026-10-09 10:00:00"
  },
  {
    "id": 2,
    "username": "bob",
    "email": "bob@example.test",
    "created_at": "2026-10-09 10:05:00",
    "updated_at": "2026-10-09 10:05:00"
  }
]
"""

    /** Список пользователей без утечки пароля — то, как API должен себя вести. */
    fun correctUserList(): MappingBuilder =
        get(urlPathEqualTo(Endpoints.USER_GET)).willReturn(json(200, CORRECT_USERS_JSON))

    /** Пустой список: граничный случай «в системе ещё нет пользователей». */
    fun emptyUserList(): MappingBuilder =
        get(urlPathEqualTo(Endpoints.USER_GET)).willReturn(json(200, "[]"))

    private fun json(status: Int, body: String) = aResponse()
        .withStatus(status)
        .withHeader("Content-Type", "application/json; charset=utf-8")
        .withBody(body.trim())
}
