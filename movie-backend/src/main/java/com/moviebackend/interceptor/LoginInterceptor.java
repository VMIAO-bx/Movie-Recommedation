package com.moviebackend.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebackend.common.Result;
import com.moviebackend.util.CurrentUserHolder;
import com.moviebackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT authentication interceptor.
 * Extracts token from Authorization header, validates it,
 * and stores the current user ID in CurrentUserHolder.
 *
 * Only authenticates requests that are mapped to actual controller methods.
 * Static resources and 404 paths are passed through to Spring's default handling.
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Handle CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Only authenticate requests that map to a real controller method.
        // Static resources and 404 paths are not HandlerMethod instances.
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // Get token from Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            writeUnauthorized(response, "未提供认证令牌");
            return false;
        }

        // Support "Bearer <token>" format or raw token
        String token;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = authHeader;
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "认证令牌无效或已过期");
            return false;
        }

        // Extract user ID and store in ThreadLocal
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            writeUnauthorized(response, "认证令牌解析失败");
            return false;
        }

        CurrentUserHolder.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // Clean up ThreadLocal to prevent memory leaks
        CurrentUserHolder.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.unauthorized(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
