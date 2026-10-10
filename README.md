# Автотесты User API

Тестовое задание: исследовать два эндпоинта, описать и приоритизировать
тест-кейсы, автоматизировать приоритетные.

Стенд: `http://18.194.45.232:3333`

- `POST /user/create` — регистрация, `multipart/form-data`: `username`, `email`, `password`
- `GET /user/get` — список зарегистрированных пользователей

### Документы задания

| Документ | Что внутри |
|---|---|
| [docs/TEST-CASES.md](docs/TEST-CASES.md) | **Части 1 и 2**: чек-лист из 64 кейсов с приоритетами P0–P3 |
| [docs/BUGS.md](docs/BUGS.md) | 18 дефектов: severity, предусловия, шаги воспроизведения, ожидаемый и фактический результат |

## Быстрый старт

```bash
# JDK 21. Если системной Java нет, но стоит Android Studio:
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew contractTest   # 2 теста на WireMock, сеть не нужна      → BUILD SUCCESSFUL
./gradlew smokeTest      # P0: 15 проходят, 1 пропущен с ID бага     → BUILD SUCCESSFUL
./gradlew regressTest    # P0–P1: 36 проходят, 10 пропущены          → BUILD SUCCESSFUL
./gradlew test           # все тесты, то же, что regressTest         → BUILD SUCCESSFUL

# Вместе с пропущенными тестами на дефекты: какие ещё воспроизводятся
./gradlew regressTest -PrunSkipped   # 10 тестов на дефекты; падают, пока дефекты не исправлены

# Отчёт Allure. Результаты накапливаются между прогонами, поэтому для чистого
# отчёта запускать вместе с clean:
./gradlew clean test allureReport   # build/reports/allure-report/allureReport/index.html
./gradlew allureServe               # тот же отчёт сразу в браузере
```

Другой стенд или локальный мок:

```bash
./gradlew regressTest -Papi.baseUrl=http://localhost:3333
```

## Задачи Gradle

| Задача | Что запускает | Тестов | Нужна сеть | Ожидаемый результат |
|---|---|---|---|---|
| `smokeTest` | тег `Smoke` — кейсы P0 | 16: 15 + 1 пропущен | да | зелёная, пока стенд жив |
| `regressTest` | тег `Regress` — кейсы P0–P1 | 46: 36 + 10 пропущено | да | зелёная |
| `test` | все тесты | 46: 36 + 10 пропущено | да | зелёная |
| `contractTest` | тег `contract` — тесты на WireMock | 2 | нет | зелёная всегда |

Флаг `-PrunSkipped` к любой задаче запускает и пропущенные тесты на дефекты.

## Теги, Allure ID и тесты на дефекты

Все тесты лежат в одном пакете [`tests`](src/test/kotlin/com/example/apitests/tests).
Состав прогонов задают теги прямо на тестах, а не папки:

```kotlin
@Test
@AllureId("TC-V15")   // ID кейса из docs/TEST-CASES.md
@Tag("Smoke")         // P0 — и в смоуке,
@Tag("Regress")       // и в регрессе
@Story("Побочные эффекты")
```

## CI

[`.github/workflows/tests.yml`](.github/workflows/tests.yml):

- `contract-tests` — на каждый push и PR. WireMock, внешних зависимостей нет,
  поэтому задание должно быть зелёным всегда.
- `api-tests` — `regressTest` против живого стенда, `continue-on-error: true`:
  стенд внешний и может быть недоступен, это не повод блокировать PR. Тесты
  на известные дефекты в нём пропускаются с ID бага.

Каждое задание выгружает артефактами JUnit-отчёт и Allure-отчёт:
`allure-report-contract` и `allure-report-regress`. Allure-отчёт собран одним
файлом `index.html`, поэтому открывается прямо из скачанного архива, без
`allureServe`.

[`.github/workflows/allure-pages.yml`](.github/workflows/allure-pages.yml) —
после прогона `tests` на `main` публикует отчёт регресса на GitHub Pages:
https://sergey-yurievich.github.io/MayFlower_QA_Test/. Тесты заново не
запускает, берёт готовый артефакт `allure-report-regress`. В PR не появляется.
