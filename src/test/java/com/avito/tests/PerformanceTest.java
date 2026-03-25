package com.avito.tests;

import com.avito.base.BaseTest;
import com.avito.models.ItemRequest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("API Avito")
@Feature("Производительность и нагрузочное тестирование")
public class PerformanceTest extends BaseTest {

    @Test
    @Story("Производительность")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-8.7: Создание 100 объявлений последовательно")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void performance_100ItemsSequential() {
        List<String> ids = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            ItemRequest request = ItemRequest.builder()
                    .sellerID(randomSellerId())
                    .name("Performance Test " + i)
                    .price(1000 + i)
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

            String id = statusMsg.substring(statusMsg.lastIndexOf(" ") + 1);
            ids.add(id);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double avgTime = totalTime / 100.0;

        System.out.println("Total time for 100 items: " + totalTime + "ms");
        System.out.println("Average time per item: " + avgTime + "ms");

        assertTrue(totalTime < 50000,
                "Общее время не должно превышать 50 секунд, получено: " + totalTime + "ms");
        assertTrue(avgTime < 500,
                "Среднее время на запрос не должно превышать 500ms, получено: " + avgTime + "ms");

        // Проверяем уникальность всех ID
        long uniqueCount = ids.stream().distinct().count();
        assertEquals(100, uniqueCount, "Все 100 ID должны быть уникальны");
    }

    @Test
    @Story("Производительность")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TC-8.8: Конкурентные запросы (10 параллельных)")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void performance_10ConcurrentRequests() throws Exception {
        int threadCount = 10;
        List<CompletableFuture<String>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            int sellerId = randomSellerId();
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                ItemRequest request = ItemRequest.builder()
                        .sellerID(sellerId)
                        .name("Concurrent Test")
                        .price(1000)
                        .statistics(ItemRequest.Statistics.builder()
                                .likes(1).viewCount(1).contacts(1).build())
                        .build();

                try {
                    String statusMsg = given().spec(spec)
                            .body(request)
                            .when().post("/api/1/item")
                            .then()
                            .statusCode(200)
                            .extract()
                            .path("status");

                    successCount.incrementAndGet();
                    return statusMsg.substring(statusMsg.lastIndexOf(" ") + 1);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    return null;
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(20, TimeUnit.SECONDS);

        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("Concurrent test completed in: " + totalTime + "ms");
        System.out.println("Success: " + successCount.get() + ", Fail: " + failCount.get());

        assertEquals(threadCount, successCount.get(),
                "Все " + threadCount + " параллельных запросов должны быть успешны");
        assertTrue(totalTime < 10000,
                "Все запросы должны выполниться за 10 секунд, получено: " + totalTime + "ms");
    }
}
