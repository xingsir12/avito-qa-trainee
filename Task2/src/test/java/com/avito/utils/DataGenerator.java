package com.avito.utils;

import com.avito.config.ApiConfig;
import com.avito.models.ItemRequest;

import java.util.Random;
import java.util.UUID;

public class DataGenerator {
    private static final Random random = new Random();

    public static int generateSellerId() {
        return ApiConfig.MIN_SELLER_ID + random.nextInt(
                ApiConfig.MAX_SELLER_ID - ApiConfig.MIN_SELLER_ID + 1
        );
    }

    public static String generateName() {
        return "Test Item " + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateName(int length) {
        return "A".repeat(Math.max(0, length));
    }

    public static String generateNameWithSpecialChars() {
        return "!@#$%^&*()_+{}[]:;'<>?,./Тест🚀";
    }

    public static String generateNameWithSQLInjection() {
        return "'; DROP TABLE items; --";
    }

    public static String generateNameWithXSS() {
        return "<script>alert('xss')</script>";
    }

    public static int generatePrice() {
        return random.nextInt(1000000) + 1;
    }

    public static ItemRequest generateValidItem() {
        return ItemRequest.builder()
                .sellerID(generateSellerId())
                .name(generateName())
                .price(generatePrice())
                .build();
    }

    public static ItemRequest generateItemWithSellerId(int sellerId) {
        return ItemRequest.builder()
                .sellerID(sellerId)
                .name(generateName())
                .price(generatePrice())
                .build();
    }

    public static ItemRequest generateItemWithStatistics() {
        return ItemRequest.builder()
                .sellerID(generateSellerId())
                .name(generateName())
                .price(generatePrice())
                .statistics(ItemRequest.Statistics.builder()
                        .likes(10)
                        .viewCount(50)
                        .contacts(5)
                        .build())
                .build();
    }

    public static String generateXSSPayload() {
        return "<script>alert('xss')</script>";
    }

    public static String generateLongName(int length) {
        return "A".repeat(Math.max(0, length));
    }

    public static ItemRequest generateItemWithNegativePrice() {
        return ItemRequest.builder()
                .sellerID(generateSellerId())
                .name(generateName())
                .price(-100)
                .statistics(ItemRequest.Statistics.builder()
                        .likes(1).viewCount(1).contacts(1).build())
                .build();
    }

    public static ItemRequest generateItemWithNegativeStatistics() {
        return ItemRequest.builder()
                .sellerID(generateSellerId())
                .name(generateName())
                .price(generatePrice())
                .statistics(ItemRequest.Statistics.builder()
                        .likes(-1).viewCount(-5).contacts(-2).build())
                .build();
    }
}