package com.example.springbootredis.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderVo {
    private Long id;  //主键

    private Long userId;  //下单的用户id

    private Long voucherId;  //购买的代金券id

    private Integer payType;  //支付方式 1：余额支付；2：支付宝；3：微信

    private Integer status;  //订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款

    private LocalDateTime createTime;  //下单时间

    private LocalDateTime payTime;  //支付时间

    private LocalDateTime useTime;  //核销时间

    private LocalDateTime refundTime;  //退款时间

    private LocalDateTime updateTime;  //更新时间

}
