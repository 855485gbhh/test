package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Table("blog_comments")
public class BlogComments  {

    @Id
    private Long id;  //主键

    private Long userId;  //用户id

    private Long blogId;  //探店id

    private Long parentId;  //关联的1级评论id，如果是一级评论，则值为0

    private Long answerId;  //回复的评论id

    private String content;  //回复的内容

    private Integer liked;  //点赞数

    private Integer status;  //状态，0：正常，1：被举报，2：禁止查看

    private Timestamp createTime;  //创建时间

    private Timestamp updateTime;  //更新时间





}

