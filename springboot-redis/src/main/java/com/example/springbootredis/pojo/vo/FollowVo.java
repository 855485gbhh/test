package com.example.springbootredis.pojo.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class FollowVo {
    private Long id;  //主键

    private Long userId;  //用户id

    private Long followUserId;  //关联的用户id


    private Boolean isFollow; //是否关注

    private Timestamp createTime;  //创建时间
}
