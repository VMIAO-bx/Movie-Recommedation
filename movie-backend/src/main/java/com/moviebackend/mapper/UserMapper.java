package com.moviebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moviebackend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper - inherits CRUD from MyBatis Plus BaseMapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
