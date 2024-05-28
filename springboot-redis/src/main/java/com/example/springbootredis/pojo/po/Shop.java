package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Table("shop")
public class Shop  {

    @Id
    private Long id;  //主键
    private String name;  //商铺名称
    private Long typeId;  //商铺类型的id
    private String images;  //商铺图片，多个图片以','隔开
    private String area;  //商圈，例如陆家嘴
    private String address;  //地址
    private String x;  //经度
    private String y;  //维度
    private Long avgPrice;  //均价，取整数
    private Integer sold;  //销量
    private Integer comments;  //评论数量
    private Integer score;  //评分，1~5分，乘10保存，避免小数
    private String openHours;  //营业时间，例如 10:00-22:00
    private Timestamp createTime;  //创建时间
    private Timestamp updateTime;  //更新时间
}

