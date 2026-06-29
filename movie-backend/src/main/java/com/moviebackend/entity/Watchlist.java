package com.moviebackend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("watchlist")
public class Watchlist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String movieId;

    private String movieTitle;

    private String moviePoster;

    /** Status: 想看 / 看过 */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
