package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Table("follow")
public class Follow  {

    @Id
    private Long id;  //主键

    private Long userId;  //用户id

    private Long followUserId;  //关联的用户id

    private Timestamp createTime;  //创建时间





}

