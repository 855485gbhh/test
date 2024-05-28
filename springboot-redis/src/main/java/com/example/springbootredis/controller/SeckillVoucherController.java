package com.example.springbootredis.controller;

import com.example.springbootredis.service.SeckillVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * 秒杀优惠券表，与优惠券是一对一关系(SeckillVoucher)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/seckillVoucher")
public class SeckillVoucherController  {

    @Autowired
    private SeckillVoucherService seckillVoucherService;
}

