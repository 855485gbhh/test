package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Table("blog")
public class Blog  {

    @Id
    private Long id;  //主键

    private Long shopId;  //商户id

    private Long userId;  //用户id

    private String title;  //标题

    private String images;  //探店的照片，最多9张，多张以","隔开

    private String content;  //探店的文字描述

    private Integer liked;  //点赞数量

    private Integer comments;  //评论数量

    private Timestamp createTime;  //创建时间

    private Timestamp updateTime;  //更新时间





}

