package com.hostel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Environment post-processor to handle Render's PostgreSQL URL format
 * Render provides DATABASE_URL in postgresql:// format but JDBC needs jdbc:postgresql://
 * This processor converts the URL before Spring Boot processes it
 */
@Component
public class DatabaseConfig implements EnvironmentPostProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            logger.info("Original DATABASE_URL: {}", maskPassword(databaseUrl));
            
            // Convert postgresql:// to jdbc:postgresql:// if needed
            String jdbcUrl = convertToJdbcUrl(databaseUrl);
            logger.info("Converted JDBC URL: {}", maskPassword(jdbcUrl));
            
            // Create a new property source with the converted URL
            Map<String, Object> dbProperties = new HashMap<>();
            dbProperties.put("spring.datasource.url", jdbcUrl);
            
            // Extract credentials from URL if present
            try {
                extractCredentialsFromUrl(databaseUrl, dbProperties);
            } catch (Exception e) {
                logger.warn("Failed to extract credentials from DATABASE_URL: {}", e.getMessage());
            }
            
            // Add the property source with high precedence
            environment.getPropertySources().addFirst(
                new MapPropertySource("convertedDatabaseUrl", dbProperties)
            );
            
            logger.info("Database URL conversion completed");
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
    private void extractCredentialsFromUrl(String databaseUrl, Map<String, Object> properties) {
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
                    
                    properties.put("spring.datasource.username", username);
                    properties.put("spring.datasource.password", password);
                    
                    logger.info("Extracted database credentials from URL for user: {}", username);
                } else {
                    // Only username, no password
                    properties.put("spring.datasource.username", credentials);
                    logger.info("Extracted database username from URL: {}", credentials);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse DATABASE_URL for credentials: {}", e.getMessage());
        }
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
