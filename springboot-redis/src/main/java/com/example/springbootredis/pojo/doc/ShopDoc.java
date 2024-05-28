package com.example.springbootredis.pojo.doc;

import com.example.springbootredis.pojo.po.Shop;
import com.example.springbootredis.pojo.vo.ShopVo;
import lombok.Data;

@Data
public class ShopDoc {
    private String id;  //主键
    private String name;  //商铺名称
    private Long typeId;  //商铺类型的id
    private String images;  //商铺图片，多个图片以','隔开
    private String area;  //商圈，例如陆家嘴
    private String address;  //地址
    private String location;  //经纬度
    private Long avgPrice;  //均价，取整数
    private Integer sold;  //销量
    private Integer comments;  //评论数量
    private Integer score;  //评分，1~5分，乘10保存，避免小数
    private String openHours;  //营业时间，例如 10:00-22:00

    public ShopDoc(Shop shop) {
        this.id = shop.getId().toString();
        this.name = shop.getName();
        this.typeId = shop.getTypeId();
        this.images = shop.getImages();
        this.area = shop.getArea();
        this.address = shop.getAddress();
        this.location =  shop.getY()+","+shop.getX();
        this.avgPrice = shop.getAvgPrice();
        this.sold = shop.getSold();
        this.comments = shop.getComments();
        this.score = shop.getScore();
        this.openHours = shop.getOpenHours();
    }
//    public ShopDoc(ShopVo shopVo) {
//        this.id = shopVo.getId().toString();
//        this.name = shopVo.getName();
//        this.typeId = shopVo.getTypeId();
//        this.images = shopVo.getImages();
//        this.area = shopVo.getArea();
//        this.address = shopVo.getAddress();
//        this.location =  shopVo.getY()+","+shopVo.getX();
//        this.avgPrice = shopVo.getAvgPrice();
//        this.sold = shopVo.getSold();
//        this.comments = shopVo.getComments();
//        this.score = shopVo.getScore();
//        this.openHours = shopVo.getOpenHours();
//    }
}
