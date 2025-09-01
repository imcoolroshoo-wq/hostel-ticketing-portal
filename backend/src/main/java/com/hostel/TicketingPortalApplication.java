package com.hostel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the Hostel Ticketing Portal.
 * This application provides a comprehensive ticketing system for managing
 * hostel-related issues and their resolution.
 */
@SpringBootApplication(
    scanBasePackages = {"com.hostel"},
    exclude = {DataSourceAutoConfiguration.class}
)
@EnableCaching
@EnableAsync
@EnableScheduling
public class TicketingPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingPortalApplication.class, args);
    }
} 