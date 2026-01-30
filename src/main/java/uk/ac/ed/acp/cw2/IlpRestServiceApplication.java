package uk.ac.ed.acp.cw2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application entry point for ILP CW2 Drone Delivery Service.
 * Starts the REST service on port 8080 and enables auto-configuration.
 */
@SpringBootApplication
public class IlpRestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IlpRestServiceApplication.class, args);
    }
}
