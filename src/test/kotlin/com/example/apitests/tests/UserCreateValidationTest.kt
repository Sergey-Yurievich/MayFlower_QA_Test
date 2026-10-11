package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.assertions.assertThatError
import com.example.apitests.config.UserFields
import com.example.apitests.data.UserFactory
import com.example.apitests.models.CreateUserRequest
import com.example.apitests.models.ErrorMessages
import com.example.apitests.models.ErrorResponse
import com.example.apitests.models.parseAs
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Issue
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import io.restassured.http.ContentType
import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Валидация обязательных полей `POST /user/create`.
 */
@Epic("User API")
@Feature("POST /user/create — валидация обязательных полей")
@DisplayName("Регистрация: валидация обязательных полей")
class UserCreateValidationTest {

    private val userApi = UserApi()

    @Test
    @AllureId("TC-V01")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться без username")
    fun `registration without username is rejected`() {
        val response = userApi.createUser(UserFactory.withUsername(null))

        assertThat(response.statusCode).describedAs("Регистрация без username не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.USERNAME_REQUIRED)
    }

    @Test
    @AllureId("TC-V02")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться без email")
    fun `registration without email is rejected`() {
        val response = userApi.createUser(UserFactory.withEmail(null))

        assertThat(response.statusCode).describedAs("Регистрация без email не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.EMAIL_REQUIRED)
    }

    @Test
    @AllureId("TC-V03")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться без password")
    fun `registration without password is rejected`() {
        val response = userApi.createUser(UserFactory.withPassword(null))

        assertThat(response.statusCode).describedAs("Регистрация без password не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.PASSWORD_REQUIRED)
    }

    @Test
    @AllureId("TC-V04")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с пустым username")
    fun `empty username is rejected`() {
        val response = userApi.createUser(UserFactory.withUsername(""))

        assertThat(response.statusCode).describedAs("Регистрация с пустым username не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.USERNAME_REQUIRED)
    }

    @Test
    @AllureId("TC-V05")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с пустым email")
    fun `empty email is rejected`() {
        val response = userApi.createUser(UserFactory.withEmail(""))

        assertThat(response.statusCode).describedAs("Регистрация с пустым email не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.EMAIL_REQUIRED)
    }

    @Test
    @AllureId("TC-V06")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с пустым password")
    fun `empty password is rejected`() {
        val response = userApi.createUser(UserFactory.withPassword(""))

        assertThat(response.statusCode).describedAs("Регистрация с пустым password не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.PASSWORD_REQUIRED)
    }

    @Test
    @AllureId("TC-V11")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться запросом без полей")
    fun `request without any field is rejected`() {
        val response = userApi.createUser(CreateUserRequest())

        assertThat(response.statusCode).describedAs("Регистрация без единого поля не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .hasAtLeastOneMessage()
    }

    @Test
    @AllureId("TC-V12")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с пустым JSON-объектом {}")
    fun `empty json object is rejected`() {
        val response = userApi.createUserWithCustomBody(ContentType.JSON.toString(), "{}")

        assertThat(response.statusCode).describedAs("Регистрация с телом {} не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.USERNAME_REQUIRED)
    }

    @Test
    @AllureId("TC-V13")
    @Tag("Regress")
    @Story("Обязательные поля")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с пустым JSON-массивом []")
    fun `empty json array is rejected`() {
        val response = userApi.createUserWithCustomBody(ContentType.JSON.toString(), "[]")

        assertThat(response.statusCode).describedAs("Регистрация с телом [] не отклонена с 400").isEqualTo(400)
        assertThatError(response.parseAs<ErrorResponse>())
            .hasSuccessFalse()
            .containsMessage(ErrorMessages.USERNAME_REQUIRED)
    }

    @Test
    @AllureId("TC-V14")
    @Tag("Regress")
    @Story("Контракт ошибки")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Ошибка валидации соответствует схеме: success=false и массив сообщений")
    fun `validation error matches schema`() {
        userApi.createUser(UserFactory.withUsername(null))
            .then()
            .statusCode(400)
            .body(matchesJsonSchemaInClasspath("schemas/error-response.json"))
    }

    @Test
    @AllureId("TC-V15")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("Побочные эффекты")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Запрос с ошибкой валидации не создаёт пользователя")
    fun `no user is created when validation fails`() {
        val request = UserFactory.withPassword(null)

        val response = userApi.createUser(request)
        assertThat(response.statusCode).describedAs("Регистрация без пароля не отклонена с 400").isEqualTo(400)

        assertThat(userApi.findUsersBy(UserFields.USERNAME, request.username!!))
            .describedAs("Отклонённый запрос всё равно создал пользователя")
            .isEmpty()
    }

    // Тесты ниже описывают правильное поведение и падают на найденных дефектах.
    // Они пропущены через @Disabled с ID бага; исправят дефект — снять @Disabled.
    @Test
    @AllureId("TC-V16")
    @Tag("Regress")
    @Disabled("BUG-04: формат email не валидируется")
    @Issue("BUG-04")
    @Story("Валидация email")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с email без @")
    fun `email without at sign must be rejected`() {
        val email = "notanemail${UserFactory.suffix()}"

        val response = userApi.createUser(UserFactory.withEmail(email))

        assertThat(response.statusCode)
            .describedAs("Регистрация с email без @ \"$email\" не отклонена с 400")
            .isEqualTo(400)
    }

    @Test
    @AllureId("TC-V17")
    @Tag("Regress")
    @Disabled("BUG-04: формат email не валидируется")
    @Issue("BUG-04")
    @Story("Валидация email")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с email без домена")
    fun `email without domain must be rejected`() {
        val email = "foo${UserFactory.suffix()}@"

        val response = userApi.createUser(UserFactory.withEmail(email))

        assertThat(response.statusCode)
            .describedAs("Регистрация с email без домена \"$email\" не отклонена с 400")
            .isEqualTo(400)
    }

    @Test
    @AllureId("TC-V18")
    @Tag("Regress")
    @Disabled("BUG-04: формат email не валидируется")
    @Issue("BUG-04")
    @Story("Валидация email")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с email без локальной части")
    fun `email without local part must be rejected`() {
        val email = "@${UserFactory.suffix()}.example.com"

        val response = userApi.createUser(UserFactory.withEmail(email))

        assertThat(response.statusCode)
            .describedAs("Регистрация с email без локальной части \"$email\" не отклонена с 400")
            .isEqualTo(400)
    }

    @Test
    @AllureId("TC-V19")
    @Tag("Regress")
    @Disabled("BUG-04: формат email не валидируется")
    @Issue("BUG-04")
    @Story("Валидация email")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с email без доменной зоны")
    fun `email without top level domain must be rejected`() {
        val email = "foo${UserFactory.suffix()}@bar"

        val response = userApi.createUser(UserFactory.withEmail(email))

        assertThat(response.statusCode)
            .describedAs("Регистрация с email без доменной зоны \"$email\" не отклонена с 400")
            .isEqualTo(400)
    }

    @Test
    @AllureId("TC-V20")
    @Tag("Regress")
    @Disabled("BUG-04: формат email не валидируется")
    @Issue("BUG-04")
    @Story("Валидация email")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с email с пробелом внутри")
    fun `email with space inside must be rejected`() {
        val email = "foo bar${UserFactory.suffix()}@example.com"

        val response = userApi.createUser(UserFactory.withEmail(email))

        assertThat(response.statusCode)
            .describedAs("Регистрация с email с пробелом внутри \"$email\" не отклонена с 400")
            .isEqualTo(400)
    }

    @Test
    @AllureId("TC-V21")
    @Tag("Regress")
    @Disabled("BUG-04: формат email не валидируется")
    @Issue("BUG-04")
    @Story("Валидация email")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с email с двумя @")
    fun `email with two at signs must be rejected`() {
        val email = "foo${UserFactory.suffix()}@@example.com"

        val response = userApi.createUser(UserFactory.withEmail(email))

        assertThat(response.statusCode)
            .describedAs("Регистрация с email с двумя @ \"$email\" не отклонена с 400")
            .isEqualTo(400)
    }

    @Test
    @AllureId("TC-V29")
    @Tag("Regress")
    @Disabled("BUG-07: дублирующееся поле username сохраняется массивом")
    @Issue("BUG-07")
    @Story("Валидация ввода")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Нельзя зарегистрироваться с двумя полями username в одной форме")
    fun `duplicated username field must be rejected`() {
        val response = userApi.createUserWithFormFields(
            listOf(
                UserFields.USERNAME to UserFactory.uniqueUsername("bug07a"),
                UserFields.USERNAME to UserFactory.uniqueUsername("bug07b"),
                UserFields.EMAIL to UserFactory.uniqueEmail("bug07"),
                UserFields.PASSWORD to "Passw0rd!2026",
            )
        )

        assertThat(response.statusCode)
            .describedAs("Регистрация с двумя полями username в одной форме не отклонена с 400")
            .isEqualTo(400)
    }

}
