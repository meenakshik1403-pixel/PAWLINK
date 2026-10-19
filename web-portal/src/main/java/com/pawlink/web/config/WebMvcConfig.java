package com.pawlink.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration registering the RoleGuardInterceptor.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RoleGuardInterceptor roleGuardInterceptor;

    public WebMvcConfig(RoleGuardInterceptor roleGuardInterceptor) {
        this.roleGuardInterceptor = roleGuardInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleGuardInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/logout", "/css/**", "/js/**", "/images/**", "/error", "/");
    }
}
