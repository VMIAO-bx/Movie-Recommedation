package com.moviebackend.controller;

import com.moviebackend.common.Result;
import com.moviebackend.dto.WatchRequest;
import com.moviebackend.entity.Watchlist;
import com.moviebackend.service.WatchlistService;
import com.moviebackend.util.CurrentUserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watch")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    /**
     * Add a movie to watchlist (想看 / 看过)
     * POST /api/watch/add
     */
    @PostMapping("/add")
    public Result<?> add(@Valid @RequestBody WatchRequest request) {
        try {
            Long userId = CurrentUserHolder.getUserId();
            Watchlist watchlist = watchlistService.addToWatchlist(userId, request);
            return Result.success("标记成功", watchlist);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Update watchlist status
     * PUT /api/watch/update
     */
    @PutMapping("/update")
    public Result<?> update(@Valid @RequestBody WatchRequest request) {
        try {
            Long userId = CurrentUserHolder.getUserId();
            Watchlist watchlist = watchlistService.updateWatchlistStatus(userId, request);
            return Result.success("更新成功", watchlist);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Remove a movie from watchlist
     * POST /api/watch/remove
     * Body: { "movieId": "xxx" }
     */
    @PostMapping("/remove")
    public Result<?> remove(@RequestBody Map<String, String> body) {
        try {
            Long userId = CurrentUserHolder.getUserId();
            String movieId = body.get("movieId");
            if (movieId == null || movieId.isEmpty()) {
                return Result.badRequest("movieId不能为空");
            }
            watchlistService.removeFromWatchlist(userId, movieId);
            return Result.success("已移除标记");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Get current user's watchlist
     * GET /api/watch/list
     */
    @GetMapping("/list")
    public Result<?> list() {
        try {
            Long userId = CurrentUserHolder.getUserId();
            List<Watchlist> watchlist = watchlistService.getWatchlist(userId);
            return Result.success(watchlist);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
