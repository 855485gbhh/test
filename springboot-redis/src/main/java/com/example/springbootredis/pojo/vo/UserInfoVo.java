package com.example.springbootredis.pojo.vo;

import lombok.Data;

import java.sql.Date;

@Data
public class UserInfoVo {
    private Long userId;  //主键，用户id

    private String city;  //城市名称

    private String introduce;  //个人介绍，不要超过128个字符

    private Integer fans;  //粉丝数量

    private Integer followee;  //关注的人的数量

    private Integer gender;  //性别，0：男，1：女

    private Date birthday;  //生日

    private Integer credits;  //积分

    private Integer level;  //会员级别，0~9级,0代表未开通会员

}
