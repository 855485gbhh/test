package com.example.springbootredis.pojo.dto;

import lombok.Data;

@Data
public class BlogDto {
    private Long id;  //主键

    private Long shopId;  //商户id

    private Long userId;  //用户id

    private String title;  //标题

    private String images;  //探店的照片，最多9张，多张以","隔开

    private String content;  //探店的文字描述
}
