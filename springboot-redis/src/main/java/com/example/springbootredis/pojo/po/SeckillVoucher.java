package com.example.springbootredis.pojo.po;


import com.mybatisflex.annotation.Id;
import lombok.Data;
import com.mybatisflex.annotation.Table;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Table("seckill_voucher")
public class SeckillVoucher  {

    @Id
    private Long voucherId;  //关联的优惠券的id

    private Integer stock;  //库存

    private LocalDateTime createTime;  //创建时间

    private LocalDateTime beginTime;  //生效时间

    private LocalDateTime endTime;  //失效时间

    private LocalDateTime updateTime;  //更新时间





}

