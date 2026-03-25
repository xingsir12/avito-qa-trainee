package com.avito.tests;

import com.avito.models.ItemResponse;
import com.avito.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("API Avito")
@Feature("GET /api/1/:sellerID/item — Все объявления продавца")
public class GetSellerItemsTest extends BaseTest {

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("TC-3.1: Все объявления продавца содержат его sellerId")
    void getSellerItems_createdItems_allHaveCorrectSellerId() {
        int sellerId = randomSellerId();
        createItem(sellerId, "Item A", 100);
        createItem(sellerId, "Item B", 200);

        ItemResponse[] items = given().spec(spec)
                .when().get("/api/1/" + sellerId + "/item")
                .then().statusCode(200)
                .extract().as(ItemResponse[].class);

        assertTrue(items.length >= 2, "Должно быть минимум 2 объявления");

        for (ItemResponse item : items) {
            assertEquals(sellerId, item.getSellerId(),
                    "Все объявления должны принадлежать продавцу " + sellerId);
        }
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-3.2: Продавец без объявлений → пустой массив")
    void getSellerItems_noItems_returnsEmptyArray() {
        int unusedSellerId = randomSellerId();

        ItemResponse[] items = given().spec(spec)
                .when().get("/api/1/" + unusedSellerId + "/item")
                .then().statusCode(200)
                .extract().as(ItemResponse[].class);

        assertEquals(0, items.length, "Ожидался пустой массив");
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-3.3: Каждый элемент списка содержит все обязательные поля")
    void getSellerItems_allItemsHaveRequiredFields() {
        int sellerId = randomSellerId();
        createItem(sellerId, "Field Check Item", 300);

        ItemResponse[] items = given().spec(spec)
                .when().get("/api/1/" + sellerId + "/item")
                .then().statusCode(200)
                .extract().as(ItemResponse[].class);

        for (ItemResponse item : items) {
            assertAll(
                    () -> assertNotNull(item.getId()),
                    () -> assertNotNull(item.getName()),
                    () -> assertNotNull(item.getPrice()),
                    () -> assertNotNull(item.getSellerId()),
                    () -> assertNotNull(item.getCreatedAt())
            );
        }
    }

    @Test
    @Story("Позитивный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-3.7: Объявления отсортированы по убыванию createdAt")
    void getSellerItems_sortedByCreatedAtDesc() throws InterruptedException {
        int sellerId = randomSellerId();

        // Создаем 3 объявления с задержкой
        ItemResponse item1 = createItem(sellerId, "First", 100);
        Thread.sleep(1000);
        ItemResponse item2 = createItem(sellerId, "Second", 200);
        Thread.sleep(1000);
        ItemResponse item3 = createItem(sellerId, "Third", 300);

        ItemResponse[] items = given()
                .spec(spec)
                .when()
                .get("/api/1/" + sellerId + "/item")
                .then()
                .statusCode(200)
                .extract()
                .as(ItemResponse[].class);

        // Проверяем, что последнее созданное (item3) идет первым
        assertTrue(items.length >= 3);
        assertEquals(item3.getId(), items[0].getId());
    }

    @Test
    @Story("Негативный")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-3.4: sellerID — строка → 400")
    void getSellerItems_stringSellerID_returns400() {
        given().spec(spec)
                .when().get("/api/1/abc/item")
                .then().statusCode(400);
    }

    @Test
    @Story("Граничный")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TC-3.6: sellerID — переполнение → не 500")
    void getSellerItems_overflowSellerID_notServerError() {
        int status = given().spec(spec)
                .when().get("/api/1/99999999999999999999/item")
                .then().extract().statusCode();

        assertNotEquals(500, status, "Не должно быть 500 при overflow sellerID");
    }
}