package com.hostel.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Database configuration to handle Render's PostgreSQL URL format
 * Render provides DATABASE_URL in postgresql:// format but JDBC needs jdbc:postgresql://
 */
@Configuration
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Value("${DATABASE_URL:}")
    private String databaseUrl;
    
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            logger.info("Original DATABASE_URL found: {}", maskPassword(databaseUrl));
            
            // Convert postgresql:// to jdbc:postgresql:// if needed
            String jdbcUrl = convertToJdbcUrl(databaseUrl);
            logger.info("Converted JDBC URL: {}", maskPassword(jdbcUrl));
            
            dataSource.setJdbcUrl(jdbcUrl);
            
            // Extract credentials from URL if present
            try {
                DatabaseCredentials credentials = extractCredentialsFromUrl(databaseUrl);
                if (credentials != null) {
                    dataSource.setUsername(credentials.username);
                    dataSource.setPassword(credentials.password);
                    logger.info("Extracted credentials for user: {}", credentials.username);
                }
            } catch (Exception e) {
                logger.warn("Failed to extract credentials from DATABASE_URL: {}", e.getMessage());
            }
        } else {
            // Fallback configuration
            dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/hostel_ticketing");
            dataSource.setUsername("postgres");
            dataSource.setPassword("password");
            logger.info("Using fallback database configuration");
        }
        
        // Configure HikariCP settings
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(300000);
        dataSource.setMaxLifetime(1200000);
        dataSource.setKeepaliveTime(30000);
        dataSource.setLeakDetectionThreshold(60000);
        dataSource.setConnectionTestQuery("SELECT 1");
        
        logger.info("Database connection configured successfully");
        return dataSource;
    }
    
    private static class DatabaseCredentials {
        String username;
        String password;
        
        DatabaseCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    
    /**
     * Convert postgresql:// URL format to jdbc:postgresql:// format
     */
    private String convertToJdbcUrl(String databaseUrl) {
        if (databaseUrl.startsWith("postgresql://")) {
            return "jdbc:" + databaseUrl;
        } else if (databaseUrl.startsWith("postgres://")) {
            // Handle postgres:// format as well
            return "jdbc:postgresql" + databaseUrl.substring("postgres".length());
        }
        return databaseUrl; // Already in correct format or unknown format
    }
    
    /**
     * Extract username and password from DATABASE_URL
     * Format: postgresql://username:password@host:port/database
     */
    private DatabaseCredentials extractCredentialsFromUrl(String databaseUrl) {
        try {
            // Remove protocol part
            String urlWithoutProtocol = databaseUrl.replaceFirst("^(postgresql|postgres)://", "");
            
            // Check if credentials are present
            if (urlWithoutProtocol.contains("@")) {
                String[] parts = urlWithoutProtocol.split("@");
                String credentials = parts[0];
                
                if (credentials.contains(":")) {
                    String[] credParts = credentials.split(":", 2);
                    String username = credParts[0];
                    String password = credParts[1];
                    
                    return new DatabaseCredentials(username, password);
                } else {
                    // Only username, no password
                    return new DatabaseCredentials(credentials, "");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse DATABASE_URL for credentials: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Mask password in URL for logging
     */
    private String maskPassword(String url) {
        if (url == null) return null;
        
        // Replace password in URL for safe logging
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }
}
