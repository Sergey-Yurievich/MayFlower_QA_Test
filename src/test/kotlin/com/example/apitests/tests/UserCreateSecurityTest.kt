package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.assertions.assertThatUser
import com.example.apitests.config.UserFields
import com.example.apitests.data.TestData.SqlInjection
import com.example.apitests.data.UserFactory
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Issue
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Проверки безопасности: инъекции, хранение пароля, утечки данных.
 *
 * Проверки, которые API *не* проходит, в конце класса: они пропущены через
 * `@Disabled` с ID бага.
 */
@Epic("User API")
@Feature("Безопасность")
@DisplayName("Безопасность: инъекции и хранение пароля")
class UserCreateSecurityTest {

    private val userApi = UserApi()

    @Test
    @AllureId("TC-SEC01")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("Хранение пароля")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/create. Пароль не возвращается в ответе открытым текстом")
    fun `password is never returned in plain text`() {
        val request = UserFactory.validUser("sec")

        val response = userApi.createUser(request)

        assertThat(response.asString())
            .describedAs("Ответ на регистрацию содержит пароль открытым текстом")
            .doesNotContain(request.password)
    }

    @Test
    @AllureId("TC-SEC03")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("SQL-инъекции")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/create. SQL-инъекция с DROP TABLE в username сохраняется как текст и не выполняется")
    fun `drop table injection in username is not executed`() {
        assertSqlInjectionStoredAsText(SqlInjection.DROP_TABLE)
    }

    @Test
    @AllureId("TC-SEC04")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("SQL-инъекции")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/create. SQL-инъекция с условием OR '1'='1' в username сохраняется как текст и не выполняется")
    fun `always true injection in username is not executed`() {
        assertSqlInjectionStoredAsText(SqlInjection.ALWAYS_TRUE)
    }

    @Test
    @AllureId("TC-SEC07")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("SQL-инъекции")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. SQL-инъекция с DROP TABLE в значении фильтра не раскрывает чужие записи")
    fun `drop table injection in query parameter is parameterized`() {
        val found = userApi.findUsersBy(UserFields.USERNAME, SqlInjection.DROP_TABLE)

        assertThat(found).describedAs("SQL-инъекция с DROP TABLE в значении фильтра вернула чужие записи").isEmpty()
    }

    @Test
    @AllureId("TC-SEC08")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("SQL-инъекции")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/get. SQL-инъекция с условием OR '1'='1' в значении фильтра не раскрывает чужие записи")
    fun `always true injection in query parameter is parameterized`() {
        val found = userApi.findUsersBy(UserFields.USERNAME, SqlInjection.ALWAYS_TRUE)

        assertThat(found).describedAs("SQL-инъекция с OR '1'='1' в значении фильтра вернула чужие записи").isEmpty()
    }

    // Тесты ниже описывают правильное поведение и падают на найденных дефектах.
    // Они пропущены через @Disabled с ID бага; исправят дефект — снять @Disabled.
    @Test
    @AllureId("TC-SEC12")
    @Tag("Smoke")
    @Tag("Regress")
    @Disabled("BUG-02: POST /user/create возвращает хеш пароля")
    @Issue("BUG-02")
    @Story("Утечка данных")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Ответ на регистрацию не содержит пароль")
    fun `create response must not contain password`() {
        val created = userApi.createUserSuccessfullyAndGetData(UserFactory.validUser("bug02"))

        assertThatUser(created).doesNotExposePassword()
    }

    @Test
    @AllureId("TC-SEC17")
    @Tag("Regress")
    @Disabled("BUG-22: клиент задаёт id и даты создания")
    @Issue("BUG-22")
    @Story("Валидация ввода")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Клиент не может задать id, created_at и updated_at")
    fun `client must not set system fields`() {
        val forgedId = -(1L..999_999_999L).random()
        val forgedTimestamp = "2000-01-01 00:00:00"

        val response = userApi.createUserWithExtraFields(
            UserFactory.validUser("bug22"),
            mapOf(
                UserFields.ID to forgedId.toString(),
                UserFields.CREATED_AT to forgedTimestamp,
                UserFields.UPDATED_AT to forgedTimestamp,
            ),
        )

        assertThat(response.statusCode)
            .describedAs("Регистрация с полями id, created_at и updated_at в форме не отклонена с 400")
            .isEqualTo(400)
    }

    /** Регистрирует пользователя с [payload] в username и проверяет, что инъекция не исполнилась. */
    private fun assertSqlInjectionStoredAsText(payload: String) {
        val username = "${UserFactory.uniqueUsername("sec")}_$payload"

        val created = userApi.createUserSuccessfullyAndGetData(UserFactory.withUsername(username))

        assertThatUser(created)
            .describedAs("SQL-инъекция в username исказила сохранённое значение")
            .hasUsername(username)

        assertThat(userApi.findUsersBy(UserFields.USERNAME, username))
            .describedAs("После SQL-инъекции запись не находится — таблица могла пострадать")
            .hasSize(1)
    }
}
