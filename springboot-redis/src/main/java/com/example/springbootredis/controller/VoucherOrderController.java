package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.VoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * (Order)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:37
 */
@RestController
@RequestMapping("/voucherOrder")
public class VoucherOrderController  {

    @Autowired
    private VoucherOrderService voucherOrderService;

    @GetMapping("/{id}")
    public JsonResponse<Object> kill(@PathVariable Long id){
        return voucherOrderService.kill(id);
    }
}

