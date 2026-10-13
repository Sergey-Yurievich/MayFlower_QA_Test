# Найденные дефекты

## Сводка

| ID | Severity | Дефект |
|---|---|---|
| [BUG-01](#bug-01) | Blocker | `GET /user/get` без авторизации отдаёт пользователей вместе с хешами паролей 
| [BUG-02](#bug-02) | Critical | `POST /user/create` возвращает хеш пароля в ответе на успешную регистрацию 
| [BUG-03](#bug-03) | Critical | `GET /user/get` отвечает `500` с текстом SQL-запроса, если передать неизвестный query-параметр 
| [BUG-22](#bug-22) | Critical | `POST /user/create` сохраняет `id`, `created_at` и `updated_at`, присланные клиентом в форме 
| [BUG-23](#bug-23) | Critical | `POST /user/create` отвечает `500` с текстом SQL-запроса и хешем пароля, если в форме незнакомое поле или `id` существующей записи 
| [BUG-04](#bug-04) | High | `POST /user/create` регистрирует пользователя с email некорректного формата 
| [BUG-05](#bug-05) | High | `POST /user/create` регистрирует пользователя с паролем из одного символа 
| [BUG-07](#bug-07) | High | `POST /user/create` сохраняет username массивом, если поле передано в форме дважды 
| [BUG-18](#bug-18) | High | `GET /user/get`: ответ крупнее ~12–34 КБ не доходит до клиента из домашней и мобильной сети без VPN — окружение стенда, не код API
| [BUG-08](#bug-08) | Medium | `POST /user/create` сохраняет HTML/JS-разметку в username как есть, без экранирования 
| [BUG-09](#bug-09) | Medium | `POST /user/create` регистрирует дубль занятого username или email, если он отличается только регистром 
| [BUG-12](#bug-12) | Medium | `POST /user/create` при битом JSON в теле возвращает ошибку HTML-страницей вместо JSON
| [BUG-13](#bug-13) | Medium | Запрос к несуществующему пути получает `404` с HTML-страницей вместо JSON 
| [BUG-14](#bug-14) | Medium | `POST /user/create` возвращает `message` строкой при успехе и массивом при ошибке 
| [BUG-11](#bug-11) | Low | `POST /user/get` возвращает список пользователей, как `GET`, вместо `405` 
| [BUG-15](#bug-15) | Low | `POST /user/create` возвращает сообщение об успешной регистрации с опечаткой `Successully` 
| [BUG-16](#bug-16) | Low | `POST /user/create` без пароля возвращает незаконченную фразу `A password for the user` 
| [BUG-24](#bug-24) | Low | `GET /user/get` отвечает `200` и `[]` вместо `400` на нечисловой `id` в фильтре 

---

## BUG-01
**`GET /user/get` без авторизации отдаёт пользователей вместе с хешами паролей**
Severity: **Blocker**

### ПУ
- Зарегистрирован пользователь, его `username` известен

### Шаги
1. Отправить `GET http://18.194.45.232:3333/user/get?username=<username из ПУ>`
   без заголовков авторизации
2. Посмотреть поля записи в ответе

### ОР
Эндпоинт требует авторизации. Поля `password` в выдаче нет.

### ФР
`200`, запись отдаётся вместе с bcrypt-хешем пароля:

```json
[{"id":1,"username":"probe_user_01","email":"probe01@test.com",
  "password":"$2a$10$4PzVX9NLbOlfAG/YXp8wb.tTR.9Km1wLCtyXFal92x6PTMH.1kURe",
  "created_at":"2026-10-09 10:17:30","updated_at":"2026-10-09 10:17:30"}]
```

Без фильтра эндпоинт так же отдаёт всю таблицу `users`.

---

## BUG-02
**`POST /user/create` возвращает хеш пароля в ответе на успешную регистрацию**
Severity: **Critical**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
2. Посмотреть блок `details` в ответе

### ОР
`200`, блок `details` не содержит поля `password`.

### ФР
`200`, `details.password` содержит bcrypt-хеш вида `$2a$10$...`.

---

## BUG-03
**`GET /user/get` отвечает `500` с текстом SQL-запроса, если передать неизвестный query-параметр**
Severity: **Critical**

### Шаги
1. Отправить `GET http://18.194.45.232:3333/user/get?limit=1&offset=0`

### ОР
Неизвестный параметр игнорируется либо запрос отклоняется с `400`. Тело
ответа не содержит ни SQL, ни имени таблицы, ни сведений о движке БД.

### ФР
`500`, в теле — SQL-запрос, имя таблицы и движок БД:

```json
{"success":false,
 "message":"Error: select * from `users` where `limit` = '1' and `offset` = '0' - SQLITE_ERROR: no such column: limit"}
```

---

## BUG-22
**`POST /user/create` сохраняет `id`, `created_at` и `updated_at`, присланные клиентом в форме**
Severity: **Critical**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
   - `id`: отрицательное число, например `-123456789`
   - `created_at`: `2000-01-01 00:00:00`
   - `updated_at`: `2000-01-01 00:00:00`
2. Посмотреть `id`, `created_at` и `updated_at` в блоке `details` ответа

### ОР
`id` и таймстемпы назначает сервер: присланные клиентом значения
игнорируются либо запрос отклоняется с `400`.

### ФР
`200`, сохранено ровно то, что прислал клиент: `id` равен присланному,
`created_at` и `updated_at` равны `2000-01-01 00:00:00`.

Положительный `id` тоже принимается и сдвигает автоинкремент для всех: после
регистрации с `id=88545477` следующий пользователь, созданный обычным
запросом, получил `id=88545478`.

---

## BUG-23
**`POST /user/create` отвечает `500` с текстом SQL-запроса и хешем пароля, если в форме незнакомое поле**
Severity: **Critical**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
   - `foo`: `bar` — поле, которого нет в API


### ОР
1. Незнакомое поле игнорируется либо запрос отклоняется с `400`.

### ФР
1. `500`, в теле — запрос `INSERT` целиком, со значениями всех полей, включая
   bcrypt-хеш пароля:

   ```json
   {"success":false,
    "message":"Error: insert into `users` (`created_at`, `email`, `foo`, `password`, `updated_at`, `username`) values ('2026-10-09 11:31:17', 'xf_1@example.com', 'bar', '$2a$10$4VeqS1eI...', '2026-10-09 11:31:17', 'xf_1') - SQLITE_ERROR: table users has no column named foo"}
   ```
---

## BUG-04
**`POST /user/create` регистрирует пользователя с email некорректного формата**
Severity: **High**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: `notanemail` — без `@`
   - `password`: `Passw0rd!2026`

### ОР
`400` с сообщением о некорректном формате адреса.

### ФР
`200` пользователь создаётся.

---

## BUG-05
**`POST /user/create` регистрирует пользователя с паролем из одного символа**
Severity: **High**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: незанятый адрес
   - `password`: `a`

### ОР
`400` с требованием минимальной длины пароля.

### ФР
`200` пользователь создаётся.

---

## BUG-07
**`POST /user/create` сохраняет username массивом, если поле передано в форме дважды**
Severity: **High**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело
   `multipart/form-data`, поля в таком порядке:
   - `username`: незанятое значение, например `dup_field_a`
   - `username`: другое незанятое значение, например `dup_field_b`
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
2. Посмотреть `details.username` в ответе

### ОР
`400` либо сохраняется одно из значений. `username` остаётся строкой.

### ФР
`200`, `username` сохранён в базу массивом из обоих значений:

```json
{"success":true,"details":{"username":["dup_field_a","dup_field_b"], ...}}
```

---

## BUG-18
**`GET /user/get`: ответ крупнее ~12–34 КБ не доходит до клиента из домашней и мобильной сети без VPN**
Severity: **High** · окружение стенда, не дефект кода API

### ПУ
- Ответ `GET http://18.194.45.232:3333/user/get` больше 34387 байт
- Клиент в домашней или мобильной сети, VPN выключен

### Шаги
1. Отправить `GET http://18.194.45.232:3333/user/get` и дочитать тело ответа
2. Сравнить число полученных байт с заголовком `Content-Length`
3. Повторить шаги 1–2 с включённым VPN

### ОР
В любой сети тело приходит целиком и совпадает с объявленным `Content-Length`.

### ФР
Сервер объявляет полный `Content-Length`, около 70 КБ, но в части сетей передача
останавливается, и клиент ждёт тело до таймаута сокета:

| Сеть клиента | Получено байт |
|---|---|
| Домашняя | 34 387 |
| Мобильная | 11 959 |
| VPN | 73 137 — весь ответ |
| GitHub Actions | весь ответ |

В каждой сети обрыв повторяется на одном и том же месте. Уменьшение MTU у клиента
не помогает: при MTU 1200 обрыв сдвигается на 27 379 байт. Проблема на уровне
сети или инфраструктуры стенда

---

## BUG-08
**`POST /user/create` сохраняет HTML/JS-разметку в username как есть, без экранирования**
Severity: **Medium**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: `<script>alert(1)</script>` с уникальным префиксом,
     например `bug08_<суффикс><script>alert(1)</script>`
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
2. Посмотреть `details.username` в ответе

### ОР
`400` из-за недопустимых символов либо разметка экранируется при сохранении.

### ФР
`200`, `details.username` содержит разметку без изменений, например
`bug08_…<script>alert(1)</script>`.

---

## BUG-09
**`POST /user/create` регистрирует дубль занятого username или email, если он отличается только регистром**
Severity: **Medium**

### ПУ
- Зарегистрирован пользователь, например `username=case_user_1`,
  `email=case_1@example.test`

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: username из ПУ в верхнем регистре — `CASE_USER_1`
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
2. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: email из ПУ в верхнем регистре — `CASE_1@EXAMPLE.TEST`
   - `password`: `Passw0rd!2026`

### ОР
1. `400` `This username is taken. Try another.`: имена, отличающиеся только
   регистром, — одно и то же имя.
2. `400` `Email already exists`: адреса, отличающиеся только регистром, —
   один и тот же ящик.

### ФР
`200` на оба запроса, создаются ещё два пользователя. Точное совпадение при
этом отклоняется с `400`.

---

## BUG-12
**`POST /user/create` при битом JSON в теле возвращает ошибку HTML-страницей вместо JSON**
Severity: **Medium**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create` с заголовком
   `Content-Type: application/json` и телом `{"username":` — JSON
   обрывается на середине
2. Посмотреть код, `Content-Type` и тело ответа

### ОР
`400`, `Content-Type: application/json`, тело в формате ошибок API:
`{"success": false, "message": [...]}`.

### ФР
`400`, `Content-Type: text/html; charset=utf-8`, тело — HTML-страница
ошибки AdonisJS.

---

## BUG-13
**Запрос к несуществующему пути получает `404` с HTML-страницей вместо JSON**
Severity: **Medium**

### Шаги
1. Отправить `GET http://18.194.45.232:3333/user/nonexistent`
2. Посмотреть код, `Content-Type` и тело ответа

### ОР
`404`, `Content-Type: application/json`.

### ФР
`404`, `Content-Type: text/html; charset=utf-8`, тело — отладочная
HTML-страница фреймворка

---

## BUG-14
**`POST /user/create` возвращает `message` строкой при успехе и массивом при ошибке**
Severity: **Medium**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
2. Посмотреть тип поля `message` в ответе
3. Отправить такой же запрос без поля `username`
4. Посмотреть тип поля `message` в ответе

### ОР
Тип `message` одинаков в обоих ответах: всегда массив либо всегда строка.

### ФР
2, успех `"User Successully created"` — строка 
4, ошибка `["A username is required"]` — массив строк 

---

## BUG-11
**`POST /user/get` возвращает список пользователей, как `GET`, вместо `405`**
Severity: **Low**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/get` без тела

### ОР
`405 Method Not Allowed`: эндпоинт только для чтения.

### ФР
`200` и список пользователей, как у `GET`. При этом `PUT`, `PATCH` и
`DELETE` на тот же путь дают `404`.

---

## BUG-15
**`POST /user/create` возвращает сообщение об успешной регистрации с опечаткой `Successully`**
Severity: **Low**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: незанятый адрес
   - `password`: `Passw0rd!2026`
2. Посмотреть поле `message` в ответе

### ОР
`"message": "User Successfully created"`.

### ФР
`"message": "User Successully created"` — в слове `Successfully` пропущена `f`.

---

## BUG-16
**`POST /user/create` без пароля возвращает незаконченную фразу `A password for the user`**
Severity: **Low**

### Шаги
1. Отправить `POST http://18.194.45.232:3333/user/create`, тело `multipart/form-data`:
   - `username`: незанятое значение
   - `email`: незанятый адрес
   - поле `password` не передавать
2. Посмотреть поле `message` в ответе

### ОР
`400`, `["A password is required"]` — по образцу сообщений
`A username is required` и `An Email is required`.

### ФР
`400`, `["A password for the user"]`.

---

## BUG-24
**`GET /user/get` отвечает `200` и `[]` вместо `400` на нечисловой `id` в фильтре**
Severity: **Low**

### Шаги
1. Отправить `GET http://18.194.45.232:3333/user/get?id=abc`
2. Отправить `GET http://18.194.45.232:3333/user/get?id=1.5`

### ОР
`400` с описанием ошибки в теле: `id` — целое число.

### ФР
`200` и `[]` на оба запроса — так же, как для несуществующего числового `id`.
