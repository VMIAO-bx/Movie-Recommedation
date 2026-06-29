-- =====================================================
-- Movie Recommendation System - Database Initialization
-- =====================================================

-- Create database
CREATE DATABASE IF NOT EXISTS movie_recommendation
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE movie_recommendation;

-- =====================================================
-- User table
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'User ID',
    `username`    VARCHAR(50)  NOT NULL                COMMENT 'Username',
    `password`    VARCHAR(255) NOT NULL                COMMENT 'Password (BCrypt encrypted)',
    `nickname`    VARCHAR(100) DEFAULT NULL            COMMENT 'Nickname',
    `avatar`      VARCHAR(500) DEFAULT NULL            COMMENT 'Avatar URL',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Registration time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- =====================================================
-- Favorite table
-- =====================================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Favorite ID',
    `user_id`      BIGINT       NOT NULL                COMMENT 'User ID',
    `movie_id`     VARCHAR(50)  NOT NULL                COMMENT 'Movie ID (from external API)',
    `movie_title`  VARCHAR(200) NOT NULL                COMMENT 'Movie title',
    `movie_poster` VARCHAR(500) DEFAULT NULL            COMMENT 'Movie poster URL',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Favorite time',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_movie_id` (`movie_id`),
    UNIQUE KEY `uk_user_movie` (`user_id`, `movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Favorite table';

-- =====================================================
-- Watchlist table (想看/看过)
-- =====================================================
DROP TABLE IF EXISTS `watchlist`;
CREATE TABLE `watchlist` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Watchlist ID',
    `user_id`      BIGINT       NOT NULL                COMMENT 'User ID',
    `movie_id`     VARCHAR(50)  NOT NULL                COMMENT 'Movie ID (from external API)',
    `movie_title`  VARCHAR(200) NOT NULL                COMMENT 'Movie title',
    `movie_poster` VARCHAR(500) DEFAULT NULL            COMMENT 'Movie poster URL',
    `status`       VARCHAR(20)  NOT NULL DEFAULT '想看' COMMENT 'Status: 想看 / 看过',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_movie_id` (`movie_id`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uk_user_movie_status` (`user_id`, `movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Watchlist table (want to watch / watched)';
