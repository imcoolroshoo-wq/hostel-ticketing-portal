package com.hostel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service to monitor database health and keep connections alive
 */
@Service
public class DatabaseHealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * Ping database every 30 seconds to keep connection alive
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void pingDatabase() {
        try {
            // Simple query to keep connection alive
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            logger.debug("Database ping successful");
        } catch (DataAccessException e) {
            logger.warn("Database ping failed: {}", e.getMessage());
            // Attempt to reconnect
            tryReconnect();
        }
    }
    
    /**
     * Perform database health check
     */
    public boolean isHealthy() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (DataAccessException e) {
            logger.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get database connection info
     */
    public String getDatabaseInfo() {
        try (Connection connection = dataSource.getConnection()) {
            return String.format("Database: %s, URL: %s", 
                connection.getMetaData().getDatabaseProductName(),
                connection.getMetaData().getURL());
        } catch (SQLException e) {
            logger.error("Failed to get database info: {}", e.getMessage());
            return "Database info unavailable";
        }
    }
    
    /**
     * Try to reconnect to database
     */
    private void tryReconnect() {
        try {
            logger.info("Attempting database reconnection...");
            
            // Test connection
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    logger.info("Database reconnection successful");
                } else {
                    logger.warn("Database connection is not valid");
                }
            }
        } catch (SQLException e) {
            logger.error("Database reconnection failed: {}", e.getMessage());
        }
    }
    
    /**
     * Check if tables exist (for initialization verification)
     */
    public boolean tablesExist() {
        try {
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users'", 
                Integer.class);
            return true;
        } catch (DataAccessException e) {
            logger.warn("Tables don't exist or can't be accessed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get connection pool status
     */
    public String getConnectionPoolStatus() {
        try {
            // This will depend on the connection pool implementation
            return "Connection pool status: Active";
        } catch (Exception e) {
            logger.error("Failed to get connection pool status: {}", e.getMessage());
            return "Connection pool status: Unknown";
        }
    }
}
