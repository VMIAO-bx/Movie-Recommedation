package com.moviebackend.controller;

import com.moviebackend.common.Result;
import com.moviebackend.dto.LoginRequest;
import com.moviebackend.dto.RegisterRequest;
import com.moviebackend.entity.User;
import com.moviebackend.service.UserService;
import com.moviebackend.util.CurrentUserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return Result.success("注册成功", user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Login
     * POST /api/user/login
     * Returns JWT token and user info
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            return Result.success("登录成功", userService.login(request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Get current user profile
     * GET /api/user/info
     */
    @GetMapping("/info")
    public Result<?> getInfo() {
        try {
            Long userId = CurrentUserHolder.getUserId();
            User user = userService.getUserById(userId);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Update user profile (nickname, avatar)
     * PUT /api/user/info
     */
    @PutMapping("/info")
    public Result<?> updateInfo(@RequestBody User request) {
        try {
            Long userId = CurrentUserHolder.getUserId();
            User updated = userService.updateUser(userId, request);
            return Result.success("修改成功", updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
