package com.avito.clients;

import com.avito.config.ApiConfig;
import com.avito.models.ItemRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiClient {

    @Step("Create item")
    public Response createItem(ItemRequest item) {
        return given()
                .spec(ApiConfig.requestSpec)
                .body(item)
                .when()
                .post("/api/1/item");
    }

    @Step("Create item with raw body")
    public Response createItem(String rawBody) {
        return given()
                .spec(ApiConfig.requestSpec)
                .body(rawBody)
                .when()
                .post("/api/1/item");
    }

    @Step("Get item by id: {id}")
    public Response getItemById(String id) {
        return given()
                .spec(ApiConfig.requestSpec)
                .when()
                .get("/api/1/item/{id}", id);
    }

    @Step("Get items by seller id: {sellerId}")
    public Response getItemsBySellerId(int sellerId) {
        return given()
                .spec(ApiConfig.requestSpec)
                .when()
                .get("/api/1/{sellerId}/item", sellerId);
    }

    @Step("Get statistics v1 by id: {id}")
    public Response getStatisticsV1(String id) {
        return given()
                .spec(ApiConfig.requestSpec)
                .when()
                .get("/api/1/statistic/{id}", id);
    }

    @Step("Get statistics v2 by id: {id}")
    public Response getStatisticsV2(String id) {
        return given()
                .spec(ApiConfig.requestSpec)
                .when()
                .get("/api/2/statistic/{id}", id);
    }

    @Step("Delete item by id: {id}")
    public Response deleteItem(String id) {
        return given()
                .spec(ApiConfig.requestSpec)
                .when()
                .delete("/api/2/item/{id}", id);
    }
}
