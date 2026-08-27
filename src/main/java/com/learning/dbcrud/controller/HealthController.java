package com.learning.dbcrud.controller;


import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.service.factory.DataServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    @Autowired
    private DataServiceFactory dataServiceFactory;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("supportedDatabases", DatabaseType.getSupportedTypes());

        // Check each database connection
        Map<String, Boolean> dbStatus = new HashMap<>();
        for (DatabaseType type : DatabaseType.values()) {
            try {
                boolean isConnected = dataServiceFactory.getService(type).isHealthy();
                dbStatus.put(type.getValue(), isConnected);
            } catch (Exception e) {
                dbStatus.put(type.getValue(), false);
            }
        }
        response.put("databaseStatus", dbStatus);

        return ResponseEntity.ok(response);
    }
}