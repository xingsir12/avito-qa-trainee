package com.avito.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemRequest {
    private Integer sellerID;
    private String name;
    private Integer price;
    private Statistics statistics;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Statistics {
        private Integer likes;
        private Integer viewCount;
        private Integer contacts;
    }
}
