package com.avito.tests;

import com.avito.models.ItemRequest;
import com.avito.base.BaseTest;
import com.avito.models.ItemResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("API Avito")
@Feature("POST /api/1/item — Создать объявление")
public class CreateItemTest extends BaseTest {

    // ===== Позитивные =====

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("TC-1.1: Создание объявления с валидными данными")
    void createItem_validData_returns200() {
        int sellerId = randomSellerId();

        ItemRequest request = ItemRequest.builder()
                .sellerID(sellerId)
                .name("Test Item")
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        // Баг сервера BUG-004: POST возвращает {"status":"Сохранили объявление - <uuid>"}
        // вместо полного объекта ItemResponse
        String statusMsg = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then()
                .statusCode(200)
                .extract().path("status");

        assertNotNull(statusMsg, "Поле status не должно быть null");
        assertTrue(statusMsg.contains("-"), "status должен содержать UUID объявления");
        String id = statusMsg.substring(statusMsg.lastIndexOf(" ") + 1);
        assertFalse(id.isBlank(), "id не должен быть пустым");
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.2: Создание объявления без блока statistics")
    void createItem_withoutStatistics_returns200or400() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("No Stats Item")
                .price(500)
                .build();

        int status = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then()
                .extract().statusCode();

        assertTrue(status == 200 || status == 400,
                "Ожидался 200 или 400, получен: " + status);
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-1.3: Два одинаковых объявления получают разные id")
    void createItem_twoIdentical_haveDifferentIds() {
        int sellerId = randomSellerId();

        ItemRequest request = ItemRequest.builder()
                .sellerID(sellerId)
                .name("Duplicate Item")
                .price(999)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        String id1 = given().spec(spec).body(request)
                .when().post("/api/1/item")
                .then().statusCode(200)
                .extract().<String>path("status")
                .replaceAll(".*- ", "");

        String id2 = given().spec(spec).body(request)
                .when().post("/api/1/item")
                .then().statusCode(200)
                .extract().<String>path("status")
                .replaceAll(".*- ", "");

        assertNotEquals(id1, id2, "id двух объявлений должны быть разными");
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.4: Создание объявления с name длиной 255 символов")
    void createItem_maxLengthName_returns200() {
        String longName = "A".repeat(255);

        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name(longName)
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        // BUG-005: сервер возвращает 400 при name длиной 255 символов
        int status = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then().extract().statusCode();

        assertEquals(200, status,
                "Ожидался 200 для name=255 символов, но сервер вернул: " + status);
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.5: Создание объявления с ненулевой статистикой")
    void createItem_nonZeroStatistics_returns200() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("Stats Item")
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(10).viewCount(50).contacts(5).build())
                .build();

        // Баг BUG-004: ответ содержит только статус-строку, не полный объект
        String statusMsg = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then().statusCode(200)
                .extract().path("status");

        assertNotNull(statusMsg);
        assertTrue(statusMsg.startsWith("Сохранили"));
    }

    // ===== Негативные =====

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-1.6: Отсутствует поле name → 400")
    void createItem_missingName_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("sellerID", randomSellerId());
        body.put("price", 1000);
        body.put("statistics", Map.of("likes", 1, "viewCount", 1, "contacts", 1));

        given().spec(spec).body(body)
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-1.7: Отсутствует поле sellerID → 400")
    void createItem_missingSellerID_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "No Seller");
        body.put("price", 1000);
        body.put("statistics", Map.of("likes", 1, "viewCount", 1, "contacts", 1));

        given().spec(spec).body(body)
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-1.8: Отсутствует поле price → 400")
    void createItem_missingPrice_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("sellerID", randomSellerId());
        body.put("name", "No Price");
        body.put("statistics", Map.of("likes", 1, "viewCount", 1, "contacts", 1));

        given().spec(spec).body(body)
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.9: Пустой JSON → 400")
    void createItem_emptyJson_returns400() {
        given().spec(spec).body("{}")
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.10: price — строка → 400")
    void createItem_priceAsString_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("sellerID", randomSellerId());
        body.put("name", "Bad Price");
        body.put("price", "тысяча");

        given().spec(spec).body(body)
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.11: sellerID — строка → 400")
    void createItem_sellerIDAsString_returns400() {
        Map<String, Object> body = new HashMap<>();
        body.put("sellerID", "abc");
        body.put("name", "Bad Seller");
        body.put("price", 1000);

        given().spec(spec).body(body)
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    // ===== Граничные =====

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.13: price = 0 → 200 или 400 (стабильно)")
    void createItem_zeroPrice_returns200Or400() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("Free Item")
                .price(0)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        int status = given().spec(spec).body(request)
                .when().post("/api/1/item")
                .then().extract().statusCode();

        assertTrue(status == 200 || status == 400,
                "Ожидался 200 или 400, получен: " + status);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.14: price = -100 → 400")
    void createItem_negativePrice_returns400() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("Negative Price")
                .price(-100)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        given().spec(spec).body(request)
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.15: name — пустая строка → 400")
    void createItem_emptyName_returns400() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("")
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        given().spec(spec).body(request)
                .when().post("/api/1/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("BUG-001: statistics с нулями → ожидается 200, сервер возвращает 400")
    void createItem_zeroStatistics_shouldReturn200_butReturns400() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("Zero Stats Item")
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(0).viewCount(0).contacts(0).build())
                .build();

        // Ожидается 200 — нулевое значение валидно для числового поля
        // Фактически сервер возвращает 400: "поле likes обязательно"
        given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then().statusCode(200);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TC-1.21: SQL injection в name — не вызывает 500")
    void createItem_sqlInjectionInName_notServerError() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("'; DROP TABLE items; --")
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        int status = given().spec(spec).body(request)
                .when().post("/api/1/item")
                .then().extract().statusCode();

        assertNotEquals(500, status, "SQL injection не должен вызывать 500");
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.23: price = Long.MAX_VALUE → не 500")
    void createItem_maxLongPrice_notServerError() {
        Map<String, Object> body = new HashMap<>();
        body.put("sellerID", randomSellerId());
        body.put("name", "Max Price Item");
        body.put("price", Long.MAX_VALUE);

        int status = given().spec(spec).body(body)
                .when().post("/api/1/item")
                .then().extract().statusCode();

        assertNotEquals(500, status, "Не должно быть 500 при price = Long.MAX_VALUE");
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.16: statistics содержит отрицательные значения")
    void createItem_negativeStatistics_returns400() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("Negative Stats")
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(-1)
                        .viewCount(-5)
                        .contacts(-2)
                        .build())
                .build();

        int status = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then().extract().statusCode();

        // Отрицательная статистика не должна приниматься
        assertEquals(400, status, "Ожидался 400 для отрицательной статистики, получен: " + status);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.17: sellerID = 0")
    void createItem_sellerIdZero_returns400() {
        ItemRequest request = ItemRequest.builder()
                .sellerID(0)
                .name("Zero Seller")
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        int status = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then().extract().statusCode();

        // sellerID должен быть в диапазоне 111111-999999
        assertEquals(400, status, "Ожидался 400 для sellerID=0, получен: " + status);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-1.22: XSS injection в name")
    void createItem_xssInjection_nameSavedAsText() {
        String xssPayload = "<script>alert('xss')</script>";

        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name(xssPayload)
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        String statusMsg = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then()
                .statusCode(200)
                .extract()
                .path("status");

        assertNotNull(statusMsg, "Статус не должен быть null");
        String id = statusMsg.substring(statusMsg.lastIndexOf(" ") + 1);

        // Проверяем, что XSS сохранен как текст, а не выполнен
        ItemResponse[] response = given().spec(spec)
                .when().get("/api/1/item/" + id)
                .then().statusCode(200)
                .extract().as(ItemResponse[].class);

        assertTrue(response.length > 0);
        assertEquals(xssPayload, response[0].getName(),
                "XSS payload должен сохраниться как текст");
    }
}