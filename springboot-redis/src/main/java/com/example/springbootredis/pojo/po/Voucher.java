package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Table("voucher")
public class Voucher  {

    @Id
    private Long id;  //主键

    private Long shopId;  //商铺id

    private String title;  //代金券标题

    private String subTitle;  //副标题

    private String rules;  //使用规则

    private Long payValue;  //支付金额，单位是分。例如200代表2元

    private Long actualValue;  //抵扣金额，单位是分。例如200代表2元

    private Integer type;  //0,普通券；1,秒杀券

    private Integer status;  //1,上架; 2,下架; 3,过期

    private LocalDateTime createTime;  //创建时间

    private LocalDateTime updateTime;  //更新时间





}

