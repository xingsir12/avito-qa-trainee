package com.avito.models;

import lombok.Data;

@Data
public class ItemResponse {
    private String id;
    private Integer sellerId;
    private String name;
    private Integer price;
    private Statistics statistics;
    private String createdAt;

    @Data
    public static class Statistics {
        private Integer likes;
        private Integer viewCount;
        private Integer contacts;
    }
}