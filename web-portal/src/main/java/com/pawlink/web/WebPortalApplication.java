package com.pawlink.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Web Portal Gateway Application on port 8081.
 * Connects to the PawLink Backend REST API (port 8080).
 */
@SpringBootApplication
public class WebPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebPortalApplication.class, args);
    }
}
