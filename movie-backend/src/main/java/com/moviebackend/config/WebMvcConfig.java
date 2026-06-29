package com.moviebackend.config;

import com.moviebackend.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration.
 * Registers the login interceptor and defines which paths are public.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // Intercept all requests
                .addPathPatterns("/api/**")
                // Exclude public endpoints (no authentication required)
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login"
                );
    }
}
