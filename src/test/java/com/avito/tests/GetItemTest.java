package com.avito.tests;

import com.avito.models.ItemResponse;
import com.avito.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("API Avito")
@Feature("GET /api/1/item/:id — Получить объявление по ID")
public class GetItemTest extends BaseTest {

    // ===== Позитивные =====

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("TC-2.1: Получение существующего объявления — данные совпадают")
    void getItem_existingId_returnsCorrectData() {
        int sellerId = randomSellerId();
        ItemResponse created = createItem(sellerId, "Get Test Item", 2000);

        ItemResponse[] response = given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then()
                .statusCode(200)
                .extract().as(ItemResponse[].class);

        assertTrue(response.length > 0, "Ответ не должен быть пустым массивом");

        ItemResponse item = response[0];
        assertAll(
                () -> assertEquals(created.getId(), item.getId()),
                () -> assertEquals("Get Test Item", item.getName()),
                () -> assertEquals(2000, item.getPrice()),
                () -> assertEquals(sellerId, item.getSellerId()),
                () -> assertNotNull(item.getCreatedAt())
        );
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-2.2: Идемпотентность GET — повторный запрос возвращает те же данные")
    void getItem_calledTwice_returnsSameData() {
        ItemResponse created = createDefaultItem();

        String response1 = given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then().statusCode(200)
                .extract().asString();

        String response2 = given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then().statusCode(200)
                .extract().asString();

        assertEquals(response1, response2, "Повторный GET должен вернуть те же данные");
    }

    // ===== Негативные =====

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-2.3: Несуществующий ID → 404")
    void getItem_nonExistentId_returns404() {
        given().spec(spec)
                .when().get("/api/1/item/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-2.4: ID не в формате UUID → 400")
    void getItem_invalidIdFormat_returns400() {
        given().spec(spec)
                .when().get("/api/1/item/abc123")
                .then().statusCode(400);
    }

    // ===== Граничные =====

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TC-2.6: ID содержит спецсимволы → 400 (нет path traversal)")
    void getItem_pathTraversal_returns400() {
        int status = given().spec(spec)
                .when().get("/api/1/item/..%2F..%2Fetc%2Fpasswd")
                .then().extract().statusCode();

        assertNotEquals(200, status, "Path traversal не должен возвращать 200");
        assertNotEquals(500, status, "Не должно быть 500");
    }
}