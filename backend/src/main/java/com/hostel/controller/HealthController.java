package com.hostel.controller;

import com.hostel.service.DatabaseHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {
    
    @Autowired
    private DatabaseHealthService databaseHealthService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Hostel Ticketing Portal");
        response.put("version", "1.0.0");
        
        // Database health
        boolean dbHealthy = databaseHealthService.isHealthy();
        response.put("database", Map.of(
            "status", dbHealthy ? "UP" : "DOWN",
            "tablesExist", databaseHealthService.tablesExist(),
            "info", databaseHealthService.getDatabaseInfo(),
            "connectionPool", databaseHealthService.getConnectionPoolStatus()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> dbStatus = new HashMap<>();
        
        boolean healthy = databaseHealthService.isHealthy();
        boolean tablesExist = databaseHealthService.tablesExist();
        
        dbStatus.put("status", healthy ? "UP" : "DOWN");
        dbStatus.put("healthy", healthy);
        dbStatus.put("tablesExist", tablesExist);
        dbStatus.put("info", databaseHealthService.getDatabaseInfo());
        dbStatus.put("connectionPool", databaseHealthService.getConnectionPoolStatus());
        dbStatus.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(dbStatus);
    }
} 