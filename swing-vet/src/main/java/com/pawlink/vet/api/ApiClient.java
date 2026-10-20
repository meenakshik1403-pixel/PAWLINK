package com.pawlink.vet.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Centralized REST API Client using Java 11+ HttpClient and Jackson ObjectMapper.
 * Automatically appends the 'X-User-Id' header from Session on authenticated requests.
 * Parses backend error responses ({ "error": "message" }) into runtime ApiExceptions.
 */
public class ApiClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Singleton instance for convenience
    private static volatile ApiClient instance;

    public ApiClient() {
        this(DEFAULT_BASE_URL);
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient();
                }
            }
        }
        return instance;
    }

    public static String get(String endpoint) {
        try {
            HttpRequest request = getInstance().createRequestBuilder(endpoint).GET().build();
            HttpResponse<String> response = getInstance().httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                throw new ApiException(response.statusCode(), getInstance().extractErrorMessage(response.body(), response.statusCode()));
            }
        } catch (IOException e) {
            throw new ApiException(503, "Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(500, "Request interrupted: " + e.getMessage());
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Prepares a HttpRequest.Builder with base headers, including 'X-User-Id' if logged in.
     */
    private HttpRequest.Builder createRequestBuilder(String endpoint) {
        String url = endpoint.startsWith("http") ? endpoint : baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");

        // Forward session user ID and role to REST backend
        Session session = Session.getInstance();
        if (session.isLoggedIn()) {
            if (session.getUserId() != null) {
                builder.header("X-User-Id", String.valueOf(session.getUserId()));
            }
            if (session.getRole() != null) {
                builder.header("X-User-Role", session.getRole());
            }
        }

        return builder;
    }

    /**
     * Executes the HTTP request and handles JSON parsing and error response mapping.
     */
    private <T> T execute(HttpRequest request, Class<T> responseClass) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                if (responseClass == null || responseClass == Void.class || responseBody == null || responseBody.trim().isEmpty()) {
                    return null;
                }
                return objectMapper.readValue(responseBody, responseClass);
            } else {
                // Parse server error response: { "error": "message" }
                String errorMessage = extractErrorMessage(responseBody, statusCode);
                throw new ApiException(statusCode, errorMessage);
            }
        } catch (IOException e) {
            throw new ApiException(503, "Network communication error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(500, "Request was interrupted: " + e.getMessage());
        }
    }

    /**
     * Parses backend error payload to extract the message, falling back to HTTP status info.
     */
    private String extractErrorMessage(String responseBody, int statusCode) {
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            try {
                JsonNode root = objectMapper.readTree(responseBody);
                if (root.has("error") && !root.get("error").isNull()) {
                    return root.get("error").asText();
                } else if (root.has("message") && !root.get("message").isNull()) {
                    return root.get("message").asText();
                }
            } catch (Exception ignored) {
                // If not JSON, return raw body if short
                if (responseBody.length() < 200) {
                    return responseBody;
                }
            }
        }
        return "Server returned HTTP status " + statusCode;
    }

    /**
     * Executes HTTP POST with JSON body.
     */
    public <T> T post(String endpoint, Object body, Class<T> responseClass) {
        try {
            String json = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = createRequestBuilder(endpoint)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return execute(request, responseClass);
        } catch (IOException e) {
            throw new ApiException(400, "Failed to serialize request payload: " + e.getMessage());
        }
    }

    /**
     * Executes HTTP POST with multipart/form-data for document uploads.
     */
    public <T> T postMultipart(String endpoint, Map<String, String> fields, Map<String, File> files, Class<T> responseClass) {
        try {
            String boundary = "----PawLinkBoundary" + System.currentTimeMillis();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Append textual fields
            if (fields != null) {
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    if (entry.getValue() == null) continue;
                    out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                }
            }

            // Append files
            if (files != null) {
                for (Map.Entry<String, File> entry : files.entrySet()) {
                    File file = entry.getValue();
                    if (file == null || !file.exists()) continue;
                    out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write("Content-Type: application/pdf\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    Files.copy(file.toPath(), out);
                    out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                }
            }

            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            byte[] bodyBytes = out.toByteArray();

            HttpRequest request = createRequestBuilder(endpoint)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                    .build();

            return execute(request, responseClass);
        } catch (IOException e) {
            throw new ApiException(400, "Failed to encode multipart request: " + e.getMessage());
        }
    }

    /**
     * Executes HTTP GET.
     */
    public <T> T get(String endpoint, Class<T> responseClass) {
        HttpRequest request = createRequestBuilder(endpoint)
                .GET()
                .build();
        return execute(request, responseClass);
    }

    /**
     * Executes HTTP GET and deserializes response into a List of elements.
     */
    public <T> List<T> getList(String endpoint, Class<T> elementClass) {
        try {
            HttpRequest request = createRequestBuilder(endpoint)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    return new ArrayList<>();
                }
                return objectMapper.readValue(
                        responseBody,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass)
                );
            } else {
                throw new ApiException(statusCode, extractErrorMessage(responseBody, statusCode));
            }
        } catch (IOException e) {
            throw new ApiException(503, "Network communication error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(500, "Request was interrupted: " + e.getMessage());
        }
    }

    /**
     * Executes HTTP PATCH with JSON body.
     */
    public <T> T patch(String endpoint, Object body, Class<T> responseClass) {
        try {
            String json = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = createRequestBuilder(endpoint)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return execute(request, responseClass);
        } catch (IOException e) {
            throw new ApiException(400, "Failed to serialize request payload: " + e.getMessage());
        }
    }

    /**
     * Executes HTTP DELETE.
     */
    public void delete(String endpoint) {
        HttpRequest request = createRequestBuilder(endpoint)
                .DELETE()
                .build();
        execute(request, Void.class);
    }
}
