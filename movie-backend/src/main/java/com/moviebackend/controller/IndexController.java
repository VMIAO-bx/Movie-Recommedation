package com.moviebackend.controller;

import com.moviebackend.common.Result;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Root endpoint and global error page for consistent JSON responses
 */
@RestController
public class IndexController implements ErrorController {

    /**
     * Root welcome endpoint
     * GET /
     */
    @RequestMapping("/")
    public Result<?> index() {
        Map<String, Object> info = new HashMap<>();
        info.put("app", "Movie Recommendation Backend");
        info.put("version", "1.0.0");
        info.put("status", "running");
        return Result.success(info);
    }

    /**
     * Handle all unmapped paths (404 etc.)
     * Returns JSON instead of the default Spring Boot error page
     */
    @RequestMapping("/error")
    public Result<?> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String message = (String) request.getAttribute("javax.servlet.error.message");
        String path = (String) request.getAttribute("javax.servlet.error.request_uri");

        if (statusCode == null) {
            statusCode = 500;
        }

        if (statusCode == 404) {
            return Result.error(404, "接口不存在: " + (path != null ? path : "unknown"));
        }

        return Result.error(statusCode, message != null ? message : "服务器内部错误");
    }
}
