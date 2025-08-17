package com.c203.autobiography.cicdTest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cicd")
@Slf4j
public class CICDTestController {


    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/ahyoon")
    public String hello(){
        return "ahyoon Test";
    }

    /**
     * 기본 헬스체크 API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "AUTOBIOGRAPHY Backend is running!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 데이터베이스 연결 테스트
     */
    @GetMapping("/test/database")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            response.put("database", "connected");
            response.put("url", connection.getMetaData().getURL());
            response.put("username", connection.getMetaData().getUserName());
        } catch (Exception e) {
            response.put("database", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Redis 연결 테스트
     */
    @GetMapping("/test/redis")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Redis 연결 테스트 시작");

            // Redis에 테스트 데이터 저장
            String key = "test:connection";
            String value = "AUTOBIOGRAPHY Redis Test - " + System.currentTimeMillis();

            log.info("Redis SET 명령 실행: key={}, value={}", key, value);
            redisTemplate.opsForValue().set(key, value);

            log.info("Redis GET 명령 실행: key={}", key);
            String retrievedValue = redisTemplate.opsForValue().get(key);

            log.info("Redis 테스트 완료: retrieved={}", retrievedValue);

            response.put("redis", "connected");
            response.put("stored", value);
            response.put("retrieved", retrievedValue);
            response.put("match", value.equals(retrievedValue));

        } catch (Exception e) {
            // 상세한 예외 로깅
            log.error("Redis 연결 테스트 실패", e);
            log.error("Exception class: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());

            response.put("redis", "error");
            response.put("error", e.getMessage());
            response.put("errorClass", e.getClass().getSimpleName());

            // 원인 예외도 확인
            if (e.getCause() != null) {
                response.put("rootCause", e.getCause().getMessage());
                response.put("rootCauseClass", e.getCause().getClass().getSimpleName());
            }

            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 전체 시스템 상태 체크
     */
    @GetMapping("/test/all")
    public ResponseEntity<Map<String, Object>> testAll() {
        Map<String, Object> response = new HashMap<>();

        // Database 테스트
        try (Connection connection = dataSource.getConnection()) {
            response.put("database_status", "OK");
        } catch (Exception e) {
            response.put("database_status", "ERROR: " + e.getMessage());
        }

        // Redis 테스트
        try {
            redisTemplate.opsForValue().set("health:check", "OK");
            response.put("redis_status", "OK");
        } catch (Exception e) {
            response.put("redis_status", "ERROR: " + e.getMessage());
        }

        // 환경 정보
        response.put("profile", System.getProperty("spring.profiles.active", "default"));
        response.put("java_version", System.getProperty("java.version"));
        response.put("server_time", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * 간단한 데이터 CRUD 테스트
     */
    @PostMapping("/test/data")
    public ResponseEntity<Map<String, Object>> createTestData(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            String key = "data:" + data.get("key");
            String value = data.get("value");

            // Redis에 저장
            redisTemplate.opsForValue().set(key, value);

            response.put("message", "Data stored successfully");
            response.put("key", key);
            response.put("value", value);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/data/{key}")
    public ResponseEntity<Map<String, Object>> getTestData(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();

        try {
            String redisKey = "data:" + key;
            String value = redisTemplate.opsForValue().get(redisKey);

            if (value != null) {
                response.put("key", key);
                response.put("value", value);
            } else {
                response.put("message", "Data not found");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

}
