package com.moviebackend.service;

import com.moviebackend.dto.LoginRequest;
import com.moviebackend.dto.RegisterRequest;
import com.moviebackend.entity.User;

import java.util.Map;

/**
 * User service interface
 */
public interface UserService {

    /**
     * Register a new user
     * @param request registration info
     * @return registered user (without password)
     */
    User register(RegisterRequest request);

    /**
     * Login and return JWT token with user info
     * @param request login credentials
     * @return map with "token" and "user" keys
     */
    Map<String, Object> login(LoginRequest request);

    /**
     * Get user by ID
     * @param userId user ID
     * @return user entity
     */
    User getUserById(Long userId);

    /**
     * Update user profile (nickname, avatar)
     * @param userId  current user ID
     * @param request updated fields
     * @return updated user
     */
    User updateUser(Long userId, User request);
}
