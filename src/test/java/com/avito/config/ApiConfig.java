package com.avito.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public class ApiConfig {
    public static final String BASE_URL = "https://qa-internship.avito.com";
    public static final int MIN_SELLER_ID = 111111;
    public static final int MAX_SELLER_ID = 999999;

    private static final AllureRestAssured allureFilter = new AllureRestAssured();

    public static RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addFilter(new RequestLoggingFilter())
            .addFilter(new ResponseLoggingFilter())
            .addFilter(allureFilter)
            .build();

    @BeforeAll
    public static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(allureFilter);
    }
}