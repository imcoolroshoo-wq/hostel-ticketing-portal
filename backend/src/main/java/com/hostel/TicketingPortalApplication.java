package com.hostel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the Hostel Ticketing Portal.
 * This application provides a comprehensive ticketing system for managing
 * hostel-related issues and their resolution.
 */
@SpringBootApplication(scanBasePackages = {"com.hostel"})
@EnableJpaRepositories(basePackages = {"com.hostel.repository"})
@EnableCaching
@EnableAsync
@EnableScheduling
public class TicketingPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingPortalApplication.class, args);
    }

    /**
     * Configuration class to conditionally exclude DataSourceAutoConfiguration
     * when DATABASE_URL is set (production environment)
     */
    @Configuration
    @ConditionalOnExpression("'${DATABASE_URL:}'.length() > 0")
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    public static class ProductionDataSourceConfig {
        // This configuration class will only be active when DATABASE_URL is set
    }
} 