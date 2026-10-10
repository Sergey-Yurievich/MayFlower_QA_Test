plugins {
    kotlin("jvm") version "2.4.21"
    id("io.qameta.allure") version "4.3.0"
}

group = "com.example.apitests"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

val junitVersion = "5.14.4"
val restAssuredVersion = "6.1.0"
val allureVersion = "2.35.5"

// CLI Allure версионируется отдельно от Java-адаптеров: версии 2.35.5 у него нет.
val allureCommandlineVersion = "2.46.1"
val wireMockVersion = "3.13.2"

dependencies {
    // --- JUnit 5 ---
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // --- HTTP-клиент ---
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.rest-assured:kotlin-extensions:$restAssuredVersion")
    testImplementation("io.rest-assured:json-schema-validator:$restAssuredVersion")

    // --- Ассерты и сериализация ---
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.22.3")

    // --- WireMock (контрактные тесты) ---
    testImplementation("org.wiremock:wiremock:$wireMockVersion")

    // --- Allure-отчёт ---
    testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
    testImplementation("io.qameta.allure:allure-junit5")
    testImplementation("io.qameta.allure:allure-rest-assured")
}

allure {
    version.set(allureCommandlineVersion)
    adapter {
        autoconfigure.set(false) // зависимости Allure подключены выше вручную
        // Без AspectJ-агента аннотации @Step не превращаются в шаги отчёта.
        aspectjWeaver.set(true)
    }
}

/**
 * Общая настройка для всех тестовых задач: платформа JUnit 5, передача baseUrl
 * в системные свойства, читаемый лог.
 */
fun Test.applyCommonSetup() {
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    systemProperty("api.baseUrl", providers.gradleProperty("api.baseUrl").get())
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.path)

    // Тесты на известные дефекты помечены @Disabled("BUG-XX: ..."). С -PrunSkipped
    // они выполняются: так проверяется, какие дефекты уже исправлены.
    if (providers.gradleProperty("runSkipped").isPresent) {
        systemProperty("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
    }

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }

    // Тесты обращаются к живому API и к WireMock — кэшировать результат нельзя.
    outputs.upToDateWhen { false }
}

tasks.test {
    description = "Полный прогон. Тесты на известные дефекты пропускаются (@Disabled с ID бага)."
    applyCommonSetup()
    useJUnitPlatform()
}

// Теги прогонов стоят прямо на тестах: P0-кейсы помечены и Smoke, и Regress,
// P1 — только Regress.
val smokeTest by tasks.registering(Test::class) {
    description = "Смоук: P0-кейсы — основной флоу и утечки данных."
    applyCommonSetup()
    useJUnitPlatform {
        includeTags("Smoke")
    }
}

val regressTest by tasks.registering(Test::class) {
    description = "Регресс: все кейсы P0–P1."
    applyCommonSetup()
    useJUnitPlatform {
        includeTags("Regress")
    }
}

val contractTest by tasks.registering(Test::class) {
    description = "Контрактные тесты на WireMock. Не требуют сети и живого сервера."
    applyCommonSetup()
    useJUnitPlatform {
        includeTags("contract")
    }
}
