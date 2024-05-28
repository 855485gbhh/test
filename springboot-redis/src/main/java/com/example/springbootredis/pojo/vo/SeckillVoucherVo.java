package com.example.springbootredis.pojo.vo;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class SeckillVoucherVo {
    private Long voucherId;  //关联的优惠券的id

    private Integer stock;  //库存

    private LocalDateTime beginTime;  //生效时间

    private LocalDateTime endTime;  //失效时间

}
