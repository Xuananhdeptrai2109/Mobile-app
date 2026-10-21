package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.entity.TestEntity;
import com.dev.expensemanagement.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    
    private final TestRepository testRepository;
    
    @GetMapping("/db-connection")
    public ResponseEntity<?> testDbConnection() {
        try {
            // Thử insert dữ liệu test
            TestEntity entity = new TestEntity();
            entity.setMessage("Database connection successful at " + System.currentTimeMillis());
            TestEntity saved = testRepository.save(entity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Database connected successfully!");
            response.put("data", saved);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "FAILED");
            response.put("message", "Database connection failed!");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is running");
        return ResponseEntity.ok(response);
    }
}
