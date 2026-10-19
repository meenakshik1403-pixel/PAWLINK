package com.pawlink.web.config;

import com.pawlink.web.dto.AuthUserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Security and Role-Guard Interceptor for the Web Portal.
 * Verifies that a valid user session exists for protected routes and
 * enforces role-based access control.
 */
@Component
public class RoleGuardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Allow public routes
        if (path.startsWith("/login") || path.startsWith("/register") || path.startsWith("/css")
                || path.startsWith("/js") || path.startsWith("/images") || path.equals("/") || path.equals("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("/login?error=Please+log+in+to+continue.");
            return false;
        }

        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");

        // Role-guard: Admin routes
        if (path.startsWith("/admin") && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect("/?error=Access+Denied:+Admin+privileges+required.");
            return false;
        }

        return true;
    }
}
