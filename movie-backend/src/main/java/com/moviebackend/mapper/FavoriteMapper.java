package com.moviebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moviebackend.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * Favorite Mapper - inherits CRUD from MyBatis Plus BaseMapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
