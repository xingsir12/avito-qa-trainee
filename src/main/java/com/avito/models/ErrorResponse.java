package com.avito.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private Result result;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private String message;
        private Object messages;
    }
}
