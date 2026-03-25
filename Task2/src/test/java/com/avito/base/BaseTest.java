package com.avito.base;

import com.avito.models.ItemRequest;
import com.avito.models.ItemResponse;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.util.Random;

import static io.restassured.RestAssured.given;

public class BaseTest {

    protected static final String BASE_URL = "https://qa-internship.avito.com";
    protected static RequestSpecification spec;
    private static final Random random = new Random();

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }

    protected int randomSellerId() {
        return 111111 + random.nextInt(888889);
    }

    // Хелпер: создать объявление и вернуть ItemResponse с id
    // Баг 1: statistics=0 → 400, workaround: используем 1
    // Баг 3: POST возвращает {"status":"Сохранили объявление - <uuid>"}
    //        вместо полного объекта — парсим id из строки
    protected ItemResponse createItem(int sellerId, String name, int price) {
        ItemRequest request = ItemRequest.builder()
                .sellerID(sellerId)
                .name(name)
                .price(price)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1)
                        .viewCount(1)
                        .contacts(1)
                        .build())
                .build();

        String statusMsg = given().spec(spec)
                .body(request)
                .when().post("/api/1/item")
                .then()
                .statusCode(200)
                .extract()
                .path("status");

        // "Сохранили объявление - d92a1e5c-b35b-46a4-9c5a-8884e54cd63f"
        String id = statusMsg.substring(statusMsg.lastIndexOf(" ") + 1);

        ItemResponse response = new ItemResponse();
        response.setId(id);
        response.setSellerId(sellerId);
        response.setName(name);
        response.setPrice(price);
        return response;
    }

    protected ItemResponse createDefaultItem() {
        return createItem(randomSellerId(), "Test Item " + System.currentTimeMillis(), 1000);
    }
}