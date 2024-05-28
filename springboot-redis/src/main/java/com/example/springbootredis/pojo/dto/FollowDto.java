package com.example.springbootredis.pojo.dto;

import lombok.Data;

@Data
public class FollowDto {
    private Long id;  //主键

    private Long userId;  //用户id

    private Long followUserId;  //关联的用户id
}
