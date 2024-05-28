package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.dto.VoucherDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.VoucherService;
import io.swagger.v3.core.util.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * (Voucher)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping("/kill")
    public JsonResponse<Object> addKill(@RequestBody VoucherDto voucherDto) {
        return voucherService.addKill(voucherDto);
    }

    @GetMapping("/{id}")
    public JsonResponse<Object> queryById(@PathVariable Long id){
        return voucherService.query(id);
    }
}

