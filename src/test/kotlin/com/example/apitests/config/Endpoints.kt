package com.example.apitests.config

/**
 * Пути API. Выносим отдельно, чтобы ни один тест не содержал строкового URI
 */
object Endpoints {
    const val USER_CREATE: String = "/user/create"
    const val USER_GET: String = "/user/get"
}

/** Имена полей формы регистрации. */
object UserFields {
    const val USERNAME: String = "username"
    const val EMAIL: String = "email"
    const val PASSWORD: String = "password"
    const val ID: String = "id"
    const val CREATED_AT: String = "created_at"
    const val UPDATED_AT: String = "updated_at"
}
