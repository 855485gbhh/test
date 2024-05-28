package com.example.springbootredis.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScrollBlogVo {
    private List<BlogVo> blogVos;  //查询到的数据
    private Long minTime;  //查询的最小时间戳
    private Integer offSet;  //偏移量（数据的score与最小时间戳相同的个数）
}
