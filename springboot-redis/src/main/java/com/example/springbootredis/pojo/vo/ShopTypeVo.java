package com.example.springbootredis.pojo.vo;

import lombok.Data;

@Data
public class ShopTypeVo {
    private Long id;  //主键

    private String name;  //类型名称

    private String icon;  //图标

    private Integer sort;  //顺序
}
