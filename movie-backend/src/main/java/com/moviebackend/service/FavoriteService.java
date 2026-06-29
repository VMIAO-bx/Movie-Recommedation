package com.moviebackend.service;

import com.moviebackend.dto.FavoriteRequest;
import com.moviebackend.entity.Favorite;

import java.util.List;

/**
 * Favorite service interface
 */
public interface FavoriteService {

    /**
     * Add a movie to favorites
     * @param userId  current user ID
     * @param request movie info
     * @return the saved favorite record
     */
    Favorite addFavorite(Long userId, FavoriteRequest request);

    /**
     * Remove a movie from favorites
     * @param userId  current user ID
     * @param movieId movie ID to remove
     */
    void removeFavorite(Long userId, String movieId);

    /**
     * Get current user's favorite list
     * @param userId current user ID
     * @return list of favorite records
     */
    List<Favorite> getFavoriteList(Long userId);
}
