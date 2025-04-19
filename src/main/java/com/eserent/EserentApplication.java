package com.eserent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the ESE-Rent application.
 * This is the entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class EserentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EserentApplication.class, args);
    }
}
