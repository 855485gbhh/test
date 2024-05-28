package com.example.springbootredis.pojo.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class VoucherVo {
    private Long id;  //主键

    private Long shopId;  //商铺id

    private String title;  //代金券标题

    private String subTitle;  //副标题

    private String rules;  //使用规则

    private Long payValue;  //支付金额，单位是分。例如200代表2元

    private Long actualValue;  //抵扣金额，单位是分。例如200代表2元

    private Integer type;  //0,普通券；1,秒杀券

    private Integer status;  //1,上架; 2,下架; 3,过期
    private SeckillVoucherVo seckillVoucherVo;

}
