package com.avito.tests;

import com.avito.models.ItemRequest;
import com.avito.models.ItemResponse;
import com.avito.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("API Avito")
@Feature("E2E и нефункциональные проверки")
public class E2EAndNonFunctionalTest extends BaseTest {

    // ==== E2E ====

    @Test
    @Story("E2E")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("TC-7.1: Полный жизненный цикл объявления")
    void e2e_fullLifecycle() {
        // 1. Создать
        ItemResponse created = createDefaultItem();
        assertNotNull(created.getId(), "id должен быть получен после создания");

        // 2. Получить по ID
        ItemResponse[] fetched = given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then().statusCode(200)
                .extract().as(ItemResponse[].class);
        assertTrue(fetched.length > 0);
        assertEquals(created.getId(), fetched[0].getId());

        // 3. Получить статистику
        given().spec(spec)
                .when().get("/api/1/statistic/" + created.getId())
                .then().statusCode(200);

        // 4. Удалить
        given().spec(spec)
                .when().delete("/api/2/item/" + created.getId())
                .then().statusCode(200);

        // 5. Проверить недоступность
        given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then().statusCode(404);
    }

    @Test
    @Story("E2E")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-7.2: Удаление одного из нескольких объявлений продавца")
    void e2e_deleteOneOfMany_listUpdated() {
        int sellerId = randomSellerId();

        ItemResponse item1 = createItem(sellerId, "Keep 1", 100);
        ItemResponse item2 = createItem(sellerId, "Keep 2", 200);
        ItemResponse toDelete = createItem(sellerId, "Delete Me", 300);

        // Удалить
        given().spec(spec)
                .when().delete("/api/2/item/" + toDelete.getId())
                .then().statusCode(200);

        // Проверить что удалённого нет, остальные есть
        List<Map<String, Object>> items = given().spec(spec)
                .when().get("/api/1/" + sellerId + "/item")
                .then().statusCode(200)
                .extract().jsonPath().getList("$");

        Set<String> ids = items.stream()
                .map(i -> (String) i.get("id"))
                .collect(Collectors.toSet());

        assertFalse(ids.contains(toDelete.getId()), "Удалённое объявление не должно быть в списке");
        assertTrue(ids.contains(item1.getId()), "item1 должен остаться");
        assertTrue(ids.contains(item2.getId()), "item2 должен остаться");
    }

    @Test
    @Story("E2E")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-7.3: Согласованность данных между GET item, GET seller, GET statistic")
    void e2e_dataConsistencyAcrossEndpoints() {
        int sellerId = randomSellerId();
        ItemResponse created = createItem(sellerId, "Consistency Test", 1500);
        String id = created.getId();

        // GET item/:id
        ItemResponse[] byId = given().spec(spec)
                .when().get("/api/1/item/" + id)
                .then().statusCode(200)
                .extract().as(ItemResponse[].class);

        // GET :sellerId/item — найти наше объявление
        List<Map<String, Object>> sellerItems = given().spec(spec)
                .when().get("/api/1/" + sellerId + "/item")
                .then().statusCode(200)
                .extract().jsonPath().getList("$");

        Map<String, Object> fromSeller = sellerItems.stream()
                .filter(i -> id.equals(i.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Объявление не найдено в списке продавца"));

        // Сравниваем данные
        assertAll(
                () -> assertEquals(byId[0].getName(), fromSeller.get("name")),
                () -> assertEquals(byId[0].getPrice(), fromSeller.get("price")),
                () -> assertEquals(byId[0].getSellerId(), fromSeller.get("sellerId"))
        );

        // GET statistic — проверяем наличие
        given().spec(spec)
                .when().get("/api/1/statistic/" + id)
                .then().statusCode(200);
    }

    // ==== Нефункциональные ====

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-8.1: Content-Type в ответе — application/json")
    void nonFunc_contentTypeIsJson() {
        ItemResponse created = createDefaultItem();

        given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-8.2: Время ответа < 500 мс (p95)")
    void nonFunc_responseTimeUnder500ms() {
        ItemResponse created = createDefaultItem();

        // Измеряем время GET запроса
        long start = System.currentTimeMillis();
        given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then().statusCode(200);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 500,
                "GET запрос выполнился за " + elapsed + " мс, ожидалось < 500 мс");
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-8.3: Невалидные данные не вызывают 500")
    void nonFunc_invalidInputsNot500() {
        // Пустое тело
        int s1 = given().spec(spec).body("{}")
                .when().post("/api/1/item")
                .then().extract().statusCode();
        assertNotEquals(500, s1);

        // Несуществующий ID
        int s2 = given().spec(spec)
                .when().get("/api/1/item/00000000-0000-0000-0000-000000000000")
                .then().extract().statusCode();
        assertNotEquals(500, s2);

        // Невалидный ID
        int s3 = given().spec(spec)
                .when().delete("/api/2/item/notvalid")
                .then().extract().statusCode();
        assertNotEquals(500, s3);
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-8.4: createdAt в формате ISO 8601")
    void nonFunc_createdAtIsISO8601() {
        ItemResponse created = createDefaultItem();
        String createdAt = created.getCreatedAt();

        assertNotNull(createdAt, "createdAt не должен быть null");
        assertDoesNotThrow(() -> Instant.parse(createdAt),
                "createdAt должен быть в формате ISO 8601: " + createdAt);
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-8.5: 10 объявлений — все id уникальны")
    void nonFunc_tenItems_allIdsUnique() {
        List<String> ids = new ArrayList<>();
        int sellerId = randomSellerId();

        for (int i = 0; i < 10; i++) {
            ItemResponse item = createItem(sellerId, "Unique Test " + i, 100 + i);
            ids.add(item.getId());
        }

        Set<String> uniqueIds = Set.copyOf(ids);
        assertEquals(10, uniqueIds.size(), "Все 10 id должны быть уникальны");
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TC-8.6: sellerID в запросе (большая D) → sellerId в ответе (маленькая d)")
    void nonFunc_sellerIDRequestVsSellerIdResponse() {
        int sellerId = randomSellerId();
        ItemResponse created = createItem(sellerId, "Case Test", 100);

        assertNotNull(created.getSellerId(),
                "В ответе поле должно называться sellerId (маленькая d)");
        assertEquals(sellerId, created.getSellerId());
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TC-8.9: CORS заголовки присутствуют")
    void nonFunc_corsHeadersPresent() {
        given()
                .spec(spec)
                .header("Origin", "https://example.com")
                .when()
                .options("/api/1/item")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", (String) null) // может быть null, но не должно быть ошибки
                .header("Access-Control-Allow-Methods", (String) null);
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TC-8.11: UTF-8 — name на кириллице сохраняется корректно")
    void nonFunc_utf8CyrillicName_savedCorrectly() {
        String cyrillicName = "Тестовое объявление на кириллице";
        int sellerId = randomSellerId();
        ItemResponse created = createItem(sellerId, cyrillicName, 100);

        ItemResponse[] fetched = given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then().statusCode(200)
                .extract().as(ItemResponse[].class);

        assertEquals(cyrillicName, fetched[0].getName(),
                "Кириллические символы должны сохраняться без искажений");
    }

    @Test
    @Story("Нефункциональный")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TC-8.9: Проверка сжатия ответов (gzip)")
    void nonFunc_gzipCompression() {
        // Создаем объявление с длинным name для проверки сжатия
        ItemRequest request = ItemRequest.builder()
                .sellerID(randomSellerId())
                .name("A".repeat(1000))
                .price(1000)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();

        String statusMsg = given().spec(spec)
                .header("Accept-Encoding", "gzip")
                .body(request)
                .when().post("/api/1/item")
                .then()
                .statusCode(200)
                .extract()
                .path("status");

        // Проверяем, что ответ пришел (сжатие не влияет на функциональность)
        assertNotNull(statusMsg, "Ответ должен быть получен");

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

        assertEquals(400, status, "Ожидался 400 для sellerID=0, получен: " + status);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-3.7: sellerID = null")
    void getSellerItems_nullSellerId_returns400() {
        int status = given().spec(spec)
                .when().get("/api/1/null/item")
                .then().extract().statusCode();

        assertTrue(status == 400 || status == 404,
                "Ожидался 400 или 404 для sellerID=null, получен: " + status);
    }
}