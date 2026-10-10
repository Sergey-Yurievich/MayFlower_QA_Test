# Автотесты User API

Тестовое задание: исследовать два эндпоинта, описать и приоритизировать
тест-кейсы, автоматизировать приоритетные.

Стенд: `http://18.194.45.232:3333`

- `POST /user/create` — регистрация, `multipart/form-data`: `username`, `email`, `password`
- `GET /user/get` — список зарегистрированных пользователей

## Быстрый старт

```bash
# JDK 21. Если системной Java нет, но стоит Android Studio:
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew smokeTest      # тег Smoke — кейсы P0
./gradlew regressTest    # тег Regress — кейсы P0–P1
./gradlew contractTest   # тег contract — тесты на WireMock, сеть не нужна
./gradlew test           # все тесты

# Отчёт Allure. Результаты накапливаются между прогонами, поэтому для чистого
# отчёта запускать вместе с clean:
./gradlew clean test allureReport   # build/reports/allure-report/allureReport/index.html
./gradlew allureServe               # тот же отчёт сразу в браузере
```

Другой стенд или локальный мок:

```bash
./gradlew regressTest -Papi.baseUrl=http://localhost:3333
```

Флаг `-PrunSkipped` к любой задаче запускает и тесты, пропущенные через
`@Disabled` с ID бага.

## CI

[`.github/workflows/tests.yml`](.github/workflows/tests.yml):

- `contract-tests` — на каждый push и PR. WireMock, внешних зависимостей нет,
  поэтому задание должно быть зелёным всегда.
- `api-tests` — `regressTest` против живого стенда, `continue-on-error: true`:
  стенд внешний и может быть недоступен, это не повод блокировать PR.

Отчёты и `allure-results` выгружаются артефактами.
