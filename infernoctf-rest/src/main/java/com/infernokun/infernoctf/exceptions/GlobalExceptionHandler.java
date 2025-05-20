package com.infernokun.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler({UsernameNotFoundException.class, RoomNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleUserNotFound(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .headers(headers)
                .body(errorResponse);
    }
}