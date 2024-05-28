package com.example.springbootredis.pojo.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BlogVo {
    private Long id;  //主键

    private Long shopId;  //商户id

    private Long userId;  //用户id

    private String title;  //标题

    private String images;  //探店的照片，最多9张，多张以","隔开

    private String content;  //探店的文字描述

    private Integer liked;  //点赞数量

    private Integer comments;  //评论数量

    private String nickName;  //昵称，默认是用户id

    private String icon;  //人物头像
    private Boolean isLike;  //是否被点赞

}
