package com.infernokun.infernoctf.utils;

import com.infernokun.infernoctf.models.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ConstFunctions {
    public static  <T> ResponseEntity<ApiResponse<T>> buildSuccessResponse(
            String message, T data, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<T>builder()
                        .code(status.value())
                        .message(message)
                        .data(data)
                        .build());
    }
}
