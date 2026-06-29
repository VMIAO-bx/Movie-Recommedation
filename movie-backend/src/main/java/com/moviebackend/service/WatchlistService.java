package com.moviebackend.service;

import com.moviebackend.dto.WatchRequest;
import com.moviebackend.entity.Watchlist;

import java.util.List;

/**
 * Watchlist service interface
 */
public interface WatchlistService {

    /**
     * Add a movie to watchlist (想看 or 看过)
     * @param userId  current user ID
     * @param request movie info and status
     * @return the saved watchlist record
     */
    Watchlist addToWatchlist(Long userId, WatchRequest request);

    /**
     * Update watchlist status (e.g., from 想看 to 看过)
     * @param userId  current user ID
     * @param request movie info with new status
     * @return updated watchlist record
     */
    Watchlist updateWatchlistStatus(Long userId, WatchRequest request);

    /**
     * Remove a movie from watchlist
     * @param userId  current user ID
     * @param movieId movie ID to remove
     */
    void removeFromWatchlist(Long userId, String movieId);

    /**
     * Get current user's watchlist
     * @param userId current user ID
     * @return list of watchlist records
     */
    List<Watchlist> getWatchlist(Long userId);
}
