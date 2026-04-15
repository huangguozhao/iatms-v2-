package com.iatms.infrastructure.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * API HTTP 客户端 - 使用 WebClient 实现
 */
@Slf4j
@Component
public class ApiClient {

    private final WebClient webClient;

    private static final int DEFAULT_TIMEOUT = 30;
    private static final int CONNECT_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 30;

    public ApiClient() {
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .build();
    }

    /**
     * 发送 HTTP 请求
     */
    public ApiResponse send(String method, String url, Map<String, String> headers, String body) {
        long startTime = System.currentTimeMillis();

        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            MediaType mediaType = MediaType.APPLICATION_JSON;

            Mono<String> responseMono = webClient.method(httpMethod)
                    .uri(url)
                    .headers(h -> {
                        if (headers != null) {
                            headers.forEach(h::set);
                        }
                        h.setContentType(mediaType);
                    })
                    .bodyValue(body != null ? body : "")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorResume(e -> {
                        log.error("HTTP请求失败: method={}, url={}, error={}", method, url, e.getMessage());
                        return Mono.just("{\"error\": \"" + e.getMessage() + "\"}");
                    });

            String responseBody = responseMono.block();
            int statusCode = 200; // 如果没有异常，默认成功

            ApiResponse response = new ApiResponse();
            response.setStatusCode(statusCode);
            response.setBody(responseBody);
            response.setResponseTime((int) (System.currentTimeMillis() - startTime));
            response.setHeaders(headers);

            log.info("HTTP请求完成: method={}, url={}, statusCode={}, duration={}ms",
                    method, url, statusCode, response.getResponseTime());

            return response;

        } catch (Exception e) {
            log.error("HTTP请求异常: method={}, url={}, error={}", method, url, e.getMessage());

            ApiResponse response = new ApiResponse();
            response.setStatusCode(500);
            response.setBody("{\"error\": \"" + e.getMessage() + "\"}");
            response.setResponseTime((int) (System.currentTimeMillis() - startTime));
            response.setHeaders(headers);

            return response;
        }
    }

    /**
     * 异步发送 HTTP 请求
     */
    public Mono<ApiResponse> sendAsync(String method, String url, Map<String, String> headers, String body) {
        long startTime = System.currentTimeMillis();

        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            MediaType mediaType = MediaType.APPLICATION_JSON;

            return webClient.method(httpMethod)
                    .uri(url)
                    .headers(h -> {
                        if (headers != null) {
                            headers.forEach(h::set);
                        }
                        h.setContentType(mediaType);
                    })
                    .bodyValue(body != null ? body : "")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .map(responseBody -> {
                        ApiResponse response = new ApiResponse();
                        response.setStatusCode(200);
                        response.setBody(responseBody);
                        response.setResponseTime((int) (System.currentTimeMillis() - startTime));
                        response.setHeaders(headers);
                        return response;
                    })
                    .onErrorResume(e -> {
                        log.error("异步HTTP请求失败: method={}, url={}, error={}", method, url, e.getMessage());
                        ApiResponse response = new ApiResponse();
                        response.setStatusCode(500);
                        response.setBody("{\"error\": \"" + e.getMessage() + "\"}");
                        response.setResponseTime((int) (System.currentTimeMillis() - startTime));
                        response.setHeaders(headers);
                        return Mono.just(response);
                    });

        } catch (Exception e) {
            log.error("异步HTTP请求异常: method={}, url={}, error={}", method, url, e.getMessage());
            ApiResponse response = new ApiResponse();
            response.setStatusCode(500);
            response.setBody("{\"error\": \"" + e.getMessage() + "\"}");
            response.setResponseTime((int) (System.currentTimeMillis() - startTime));
            response.setHeaders(headers);
            return Mono.just(response);
        }
    }

    /**
     * 发送 GET 请求
     */
    public ApiResponse get(String url, Map<String, String> headers) {
        return send("GET", url, headers, null);
    }

    /**
     * 发送 POST 请求
     */
    public ApiResponse post(String url, Map<String, String> headers, String body) {
        return send("POST", url, headers, body);
    }

    /**
     * 发送 PUT 请求
     */
    public ApiResponse put(String url, Map<String, String> headers, String body) {
        return send("PUT", url, headers, body);
    }

    /**
     * 发送 DELETE 请求
     */
    public ApiResponse delete(String url, Map<String, String> headers) {
        return send("DELETE", url, headers, null);
    }

    /**
     * API 响应
     */
    @lombok.Data
    public static class ApiResponse {
        private int statusCode;
        private String body;
        private int responseTime;
        private Map<String, String> headers;

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        public boolean is4xxError() {
            return statusCode >= 400 && statusCode < 500;
        }

        public boolean is5xxError() {
            return statusCode >= 500;
        }
    }
}
