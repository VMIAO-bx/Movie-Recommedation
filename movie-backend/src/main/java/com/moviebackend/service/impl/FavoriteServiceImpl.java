package com.moviebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moviebackend.dto.FavoriteRequest;
import com.moviebackend.entity.Favorite;
import com.moviebackend.mapper.FavoriteMapper;
import com.moviebackend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public Favorite addFavorite(Long userId, FavoriteRequest request) {
        // Check if already favorited
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getMovieId, request.getMovieId());
        if (favoriteMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该电影已收藏");
        }

        // Create favorite record
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setMovieId(request.getMovieId());
        favorite.setMovieTitle(request.getMovieTitle());
        favorite.setMoviePoster(request.getMoviePoster());

        favoriteMapper.insert(favorite);
        return favorite;
    }

    @Override
    public void removeFavorite(Long userId, String movieId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getMovieId, movieId);
        int deleted = favoriteMapper.delete(wrapper);
        if (deleted == 0) {
            throw new RuntimeException("收藏记录不存在");
        }
    }

    @Override
    public List<Favorite> getFavoriteList(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getCreateTime);
        return favoriteMapper.selectList(wrapper);
    }
}
