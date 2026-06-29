package com.moviebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moviebackend.entity.Watchlist;
import org.apache.ibatis.annotations.Mapper;

/**
 * Watchlist Mapper - inherits CRUD from MyBatis Plus BaseMapper
 */
@Mapper
public interface WatchlistMapper extends BaseMapper<Watchlist> {
}
