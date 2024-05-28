package com.example.springbootredis.pojo.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UserDto {
    private Long id;  //主键

    private String nickName;  //昵称，默认是用户id

    private String icon;  //人物头像

}
