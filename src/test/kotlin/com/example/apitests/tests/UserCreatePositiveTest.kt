package com.example.apitests.tests

import com.example.apitests.api.UserApi
import com.example.apitests.assertions.assertThatUser
import com.example.apitests.data.UserFactory
import com.example.apitests.models.CreateUserSuccessResponse
import com.example.apitests.models.parseAs
import io.qameta.allure.AllureId
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Позитивные сценарии регистрации против реального API.
 */
@Epic("User API")
@Feature("POST /user/create — позитивные сценарии")
@DisplayName("Регистрация: позитивные сценарии")
class UserCreatePositiveTest {

    private val userApi = UserApi()

    @Test
    @AllureId("TC-P01")
    @Tag("Smoke")
    @Tag("Regress")
    @Story("Создание пользователя")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Эндпоинт /user/create. Ответ на регистрацию содержит id, переданные username и email, created_at и updated_at")
    fun `successful registration returns created entity`() {
        val request = UserFactory.validUser()

        val created = userApi.createUserSuccessfullyAndGetData(request)

        assertThatUser(created)
            .hasAssignedId()
            .hasUsernameAndEmailFrom(request)
            .hasCreatedAtAndUpdatedAt()
    }

    @Test
    @AllureId("TC-P02")
    @Tag("Regress")
    @Story("Создание пользователя")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Успешный ответ содержит success=true и непустое message")
    fun `successful response carries success flag and message`() {
        val response = userApi.createUser(UserFactory.validUser())

        val parsed = response.parseAs<CreateUserSuccessResponse>()
        assertThat(parsed.success).describedAs("Успешная регистрация вернула success != true").isTrue()
        assertThat(parsed.message).describedAs("Успешная регистрация вернула пустое message").isNotBlank()
    }

    @Test
    @AllureId("TC-P03")
    @Tag("Regress")
    @Story("Создание пользователя")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Эндпоинт /user/create. Разным пользователям присваиваются разные id")
    fun `different users get different ids`() {
        val first = userApi.createUserSuccessfullyAndGetData(UserFactory.validUser())
        val second = userApi.createUserSuccessfullyAndGetData(UserFactory.validUser())

        assertThat(second.id)
            .describedAs("Двум разным пользователям присвоен один и тот же id")
            .isNotEqualTo(first.id)
    }
}
