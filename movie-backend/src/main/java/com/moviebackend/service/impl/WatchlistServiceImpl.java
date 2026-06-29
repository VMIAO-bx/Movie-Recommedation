package com.moviebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moviebackend.dto.WatchRequest;
import com.moviebackend.entity.Watchlist;
import com.moviebackend.mapper.WatchlistMapper;
import com.moviebackend.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    @Autowired
    private WatchlistMapper watchlistMapper;

    @Override
    public Watchlist addToWatchlist(Long userId, WatchRequest request) {
        // Check if already in watchlist
        LambdaQueryWrapper<Watchlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Watchlist::getUserId, userId)
               .eq(Watchlist::getMovieId, request.getMovieId());
        if (watchlistMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该电影已在标记列表中，请使用更新接口修改状态");
        }

        // Validate status
        String status = request.getStatus();
        if (status == null || (!status.equals("想看") && !status.equals("看过"))) {
            throw new RuntimeException("状态值无效，请使用 '想看' 或 '看过'");
        }

        // Create watchlist record
        Watchlist watchlist = new Watchlist();
        watchlist.setUserId(userId);
        watchlist.setMovieId(request.getMovieId());
        watchlist.setMovieTitle(request.getMovieTitle());
        watchlist.setMoviePoster(request.getMoviePoster());
        watchlist.setStatus(status);

        watchlistMapper.insert(watchlist);
        return watchlist;
    }

    @Override
    public Watchlist updateWatchlistStatus(Long userId, WatchRequest request) {
        // Find existing record
        LambdaQueryWrapper<Watchlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Watchlist::getUserId, userId)
               .eq(Watchlist::getMovieId, request.getMovieId());
        Watchlist watchlist = watchlistMapper.selectOne(wrapper);

        if (watchlist == null) {
            throw new RuntimeException("标记记录不存在");
        }

        // Validate status
        String status = request.getStatus();
        if (status == null || (!status.equals("想看") && !status.equals("看过"))) {
            throw new RuntimeException("状态值无效，请使用 '想看' 或 '看过'");
        }

        watchlist.setStatus(status);
        watchlistMapper.updateById(watchlist);
        return watchlist;
    }

    @Override
    public void removeFromWatchlist(Long userId, String movieId) {
        LambdaQueryWrapper<Watchlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Watchlist::getUserId, userId)
               .eq(Watchlist::getMovieId, movieId);
        Watchlist watchlist = watchlistMapper.selectOne(wrapper);
        if (watchlist == null) {
            throw new RuntimeException("标记记录不存在");
        }
        watchlistMapper.deleteById(watchlist.getId());
    }

    @Override
    public List<Watchlist> getWatchlist(Long userId) {
        LambdaQueryWrapper<Watchlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Watchlist::getUserId, userId)
               .orderByDesc(Watchlist::getCreateTime);
        return watchlistMapper.selectList(wrapper);
    }
}
