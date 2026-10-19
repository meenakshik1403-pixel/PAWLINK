package com.pawlink.web.api;

import com.pawlink.web.dto.AuthUserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Gateway Pattern Implementation using Spring 6 RestClient.
 * Fetches the logged-in user from HttpSession and automatically forwards
 * the 'X-User-Id' HTTP header to the backend REST API on port 8080.
 */
@Component
public class ApiGateway {

    private final RestClient restClient;

    public ApiGateway(@Value("${pawlink.api.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Resolves the current session user ID if available in HttpSession.
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object userObj = session.getAttribute("user");
                if (userObj instanceof AuthUserDTO user) {
                    return user.getId();
                }
            }
        }
        return null;
    }

    /**
     * Forwarding GET request with optional X-User-Id header.
     */
    public <T> T get(String uri, Class<T> responseType) {
        Long userId = getCurrentUserId();
        return restClient.get()
                .uri(uri)
                .headers(headers -> {
                    headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
                    if (userId != null) {
                        headers.set("X-User-Id", String.valueOf(userId));
                    }
                })
                .retrieve()
                .body(responseType);
    }

    /**
     * Forwarding POST request with body and X-User-Id header.
     */
    public <T> T post(String uri, Object body, Class<T> responseType) {
        Long userId = getCurrentUserId();
        return restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
                    if (userId != null) {
                        headers.set("X-User-Id", String.valueOf(userId));
                    }
                })
                .body(body)
                .retrieve()
                .body(responseType);
    }

    /**
     * Forwarding PATCH request with body and X-User-Id header.
     */
    public <T> T patch(String uri, Object body, Class<T> responseType) {
        Long userId = getCurrentUserId();
        return restClient.patch()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
                    if (userId != null) {
                        headers.set("X-User-Id", String.valueOf(userId));
                    }
                })
                .body(body)
                .retrieve()
                .body(responseType);
    }

    /**
     * Forwarding DELETE request.
     */
    public void delete(String uri) {
        Long userId = getCurrentUserId();
        restClient.delete()
                .uri(uri)
                .headers(headers -> {
                    if (userId != null) {
                        headers.set("X-User-Id", String.valueOf(userId));
                    }
                })
                .retrieve()
                .toBodilessEntity();
    }
}
