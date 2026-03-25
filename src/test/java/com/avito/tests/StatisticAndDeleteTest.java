package com.avito.tests;

import com.avito.models.ItemResponse;
import com.avito.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("API Avito")
@Feature("Статистика и удаление объявлений")
public class StatisticAndDeleteTest extends BaseTest {

    // ==== Статистика v1 ====

    @Test
    @Story("GET /api/1/statistic/:id")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-4.1: Статистика возвращает корректные значения")
    void getStatisticV1_existingItem_returnsCorrectStats() {
        ItemResponse created = createDefaultItem();

        List<Map<String, Object>> stats = given().spec(spec)
                .when().get("/api/1/statistic/" + created.getId())
                .then().statusCode(200)
                .extract().jsonPath().getList("$");

        assertFalse(stats.isEmpty(), "Статистика не должна быть пустой");
        Map<String, Object> stat = stats.get(0);
        assertAll(
                () -> assertTrue(stat.containsKey("likes"), "Нет поля likes"),
                () -> assertTrue(stat.containsKey("viewCount"), "Нет поля viewCount"),
                () -> assertTrue(stat.containsKey("contacts"), "Нет поля contacts")
        );
    }

    @Test
    @Story("GET /api/1/statistic/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-4.2: Идемпотентность GET statistic")
    void getStatisticV1_calledTwice_returnsSameData() {
        ItemResponse created = createDefaultItem();

        String r1 = given().spec(spec)
                .when().get("/api/1/statistic/" + created.getId())
                .then().statusCode(200).extract().asString();

        String r2 = given().spec(spec)
                .when().get("/api/1/statistic/" + created.getId())
                .then().statusCode(200).extract().asString();

        assertEquals(r1, r2);
    }

    @Test
    @Story("GET /api/1/statistic/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-4.3: Статистика несуществующего ID → 404")
    void getStatisticV1_nonExistentId_returns404() {
        given().spec(spec)
                .when().get("/api/1/statistic/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }

    @Test
    @Story("GET /api/1/statistic/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-4.4: Статистика с невалидным ID → 400")
    void getStatisticV1_invalidId_returns400() {
        given().spec(spec)
                .when().get("/api/1/statistic/notauuid")
                .then().statusCode(400);
    }

    // ==== Статистика v2 ====

    @Test
    @Story("GET /api/2/statistic/:id")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-5.1: Статистика v2 возвращает 200")
    void getStatisticV2_existingItem_returns200() {
        ItemResponse created = createDefaultItem();

        given().spec(spec)
                .when().get("/api/2/statistic/" + created.getId())
                .then().statusCode(200);
    }

    @Test
    @Story("GET /api/2/statistic/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-5.2: Данные статистики v1 и v2 совпадают")
    void getStatisticV1AndV2_sameItem_returnSameData() {
        ItemResponse created = createDefaultItem();

        String v1 = given().spec(spec)
                .when().get("/api/1/statistic/" + created.getId())
                .then().statusCode(200).extract().asString();

        String v2 = given().spec(spec)
                .when().get("/api/2/statistic/" + created.getId())
                .then().statusCode(200).extract().asString();

        assertEquals(v1, v2, "Данные v1 и v2 должны совпадать");
    }

    @Test
    @Story("GET /api/2/statistic/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-5.3: Статистика v2 несуществующего ID → 404")
    void getStatisticV2_nonExistentId_returns404() {
        given().spec(spec)
                .when().get("/api/2/statistic/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }

    // ==== DELETE ====

    @Test
    @Story("DELETE /api/2/item/:id")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("TC-6.1: Удаление существующего объявления → 200")
    void deleteItem_existingItem_returns200() {
        ItemResponse created = createDefaultItem();

        given().spec(spec)
                .when().delete("/api/2/item/" + created.getId())
                .then().statusCode(200);
    }

    @Test
    @Story("DELETE /api/2/item/:id")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("TC-6.2: После удаления GET возвращает 404")
    void deleteItem_thenGet_returns404() {
        ItemResponse created = createDefaultItem();

        given().spec(spec)
                .when().delete("/api/2/item/" + created.getId())
                .then().statusCode(200);

        given().spec(spec)
                .when().get("/api/1/item/" + created.getId())
                .then().statusCode(404);
    }

    @Test
    @Story("DELETE /api/2/item/:id")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TC-6.3: После удаления объявление отсутствует в списке продавца")
    void deleteItem_thenGetSellerItems_itemAbsent() {
        int sellerId = randomSellerId();
        ItemResponse created = createItem(sellerId, "To Delete", 100);

        given().spec(spec)
                .when().delete("/api/2/item/" + created.getId())
                .then().statusCode(200);

        List<Map<String, Object>> items = given().spec(spec)
                .when().get("/api/1/" + sellerId + "/item")
                .then().statusCode(200)
                .extract().jsonPath().getList("$");

        boolean found = items.stream()
                .anyMatch(i -> created.getId().equals(i.get("id")));

        assertFalse(found, "Удалённое объявление не должно быть в списке продавца");
    }

    @Test
    @Story("DELETE /api/2/item/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-6.4: Повторное удаление → не 500")
    void deleteItem_twice_notServerError() {
        ItemResponse created = createDefaultItem();

        given().spec(spec)
                .when().delete("/api/2/item/" + created.getId())
                .then().statusCode(200);

        int status = given().spec(spec)
                .when().delete("/api/2/item/" + created.getId())
                .then().extract().statusCode();

        assertNotEquals(500, status, "Повторное удаление не должно вызывать 500");
    }

    @Test
    @Story("DELETE /api/2/item/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-6.5: Удаление несуществующего ID → 404")
    void deleteItem_nonExistentId_returns404() {
        given().spec(spec)
                .when().delete("/api/2/item/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }

    @Test
    @Story("DELETE /api/2/item/:id")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-6.6: Невалидный ID при удалении → 400")
    void deleteItem_invalidId_returns400() {
        given().spec(spec)
                .when().delete("/api/2/item/notvalid")
                .then().statusCode(400);
    }
}