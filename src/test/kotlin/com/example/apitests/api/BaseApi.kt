package com.example.apitests.api

import com.example.apitests.config.TestConfig
import com.example.apitests.specs.Specs
import io.restassured.RestAssured
import io.restassured.specification.RequestSpecification
abstract class BaseApi(protected val baseUrl: String = TestConfig.baseUrl) {

    protected fun request(): RequestSpecification = RestAssured.given().spec(Specs.requestSpec(baseUrl))
}
