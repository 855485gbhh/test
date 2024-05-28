package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Table("user")
public class User  {

    @Id(keyType = KeyType.Auto)
    private Long id;  //主键

    private String phone;  //手机号码

    private String password;  //密码，加密存储

    private String nickName;  //昵称，默认是用户id

    private String icon;  //人物头像

    private Timestamp createTime;  //创建时间

    private Timestamp updateTime;  //更新时间





}

