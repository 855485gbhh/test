package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Table("shop_type")
public class ShopType  {

    @Id
    private Long id;  //主键

    private String name;  //类型名称

    private String icon;  //图标

    private Integer sort;  //顺序

    private Timestamp createTime;  //创建时间

    private Timestamp updateTime;  //更新时间





}

