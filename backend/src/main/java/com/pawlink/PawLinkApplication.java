package com.pawlink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PawLink Spring Boot Application entrypoint.
 * Automatically loads .env configuration from workspace root if present.
 */
@SpringBootApplication
public class PawLinkApplication {

    private static final Logger log = LoggerFactory.getLogger(PawLinkApplication.class);

    public static void main(String[] args) {
        loadDotEnvIfExists();
        SpringApplication.run(PawLinkApplication.class, args);
    }

    /**
     * Reads .env file from working directory or parent directory if present,
     * populating System properties so Spring Boot and application.properties
     * pick up environment variables (Supabase URL, Admin credentials, etc.).
     */
    public static void loadDotEnvIfExists() {
        Path[] candidatePaths = new Path[]{
                Paths.get(".env"),
                Paths.get("../.env"),
                Paths.get("../../.env")
        };

        for (Path envPath : candidatePaths) {
            if (Files.exists(envPath) && Files.isRegularFile(envPath)) {
                log.info("Loading environment configuration from: {}", envPath.toAbsolutePath().normalize());
                try (BufferedReader reader = Files.newBufferedReader(envPath)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                                if (value.length() >= 2) {
                                    value = value.substring(1, value.length() - 1);
                                }
                            }
                            if (System.getProperty(key) == null && System.getenv(key) == null && !value.isEmpty()) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                } catch (IOException e) {
                    log.warn("Could not read .env file at {}: {}", envPath, e.getMessage());
                }
                break;
            }
        }
    }
}
