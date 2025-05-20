package com.infernokun.infernoctf.config.feign;


import feign.HeaderMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "some-server",
    configuration = InfernoCTFFeignClientConfig.class)
public interface InfernoCTFFeignClient {
    @PostMapping(value = "/api/example", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> something (@RequestParam String somethings, @HeaderMap Map<String, String> headerMap);

    @PostMapping(value = "/api/example", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> something2(@RequestParam("api-keu") String api, @PathVariable String somethings, @HeaderMap Map<String, String> headerMap);
}
