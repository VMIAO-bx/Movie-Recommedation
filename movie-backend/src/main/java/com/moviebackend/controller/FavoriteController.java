package com.moviebackend.controller;

import com.moviebackend.common.Result;
import com.moviebackend.dto.FavoriteRequest;
import com.moviebackend.entity.Favorite;
import com.moviebackend.service.FavoriteService;
import com.moviebackend.util.CurrentUserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * Add a movie to favorites
     * POST /api/favorite/add
     */
    @PostMapping("/add")
    public Result<?> add(@Valid @RequestBody FavoriteRequest request) {
        try {
            Long userId = CurrentUserHolder.getUserId();
            Favorite favorite = favoriteService.addFavorite(userId, request);
            return Result.success("收藏成功", favorite);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Remove a movie from favorites
     * POST /api/favorite/remove
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
            favoriteService.removeFavorite(userId, movieId);
            return Result.success("取消收藏成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Get current user's favorite list
     * GET /api/favorite/list
     */
    @GetMapping("/list")
    public Result<?> list() {
        try {
            Long userId = CurrentUserHolder.getUserId();
            List<Favorite> favorites = favoriteService.getFavoriteList(userId);
            return Result.success(favorites);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
