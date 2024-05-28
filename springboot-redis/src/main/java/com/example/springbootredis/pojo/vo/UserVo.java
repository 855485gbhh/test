package com.example.springbootredis.pojo.vo;

import com.example.springbootredis.pojo.po.UserInfo;
import lombok.Data;

import java.util.List;

@Data
public class UserVo {
    private Long id;  //主键

    private String phone;  //手机号码

    private String password;  //密码，加密存储

    private String nickName;  //昵称，默认是用户id

    private String icon;  //人物头像
    private UserInfoVo userInfoVo;
    private List<BlogVo> blogVos;
}
