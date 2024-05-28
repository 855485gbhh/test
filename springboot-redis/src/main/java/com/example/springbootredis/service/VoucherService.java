package com.example.springbootredis.service;

import com.example.springbootredis.pojo.dto.VoucherDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.SeckillVoucherVo;
import com.example.springbootredis.pojo.vo.VoucherVo;

/**
 * (Voucher)表服务接口
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
public interface VoucherService  {

    JsonResponse<Object> addKill(VoucherDto voucherDto);

    JsonResponse<Object> query(Long id);

    VoucherVo queryById(Long id);

    boolean decreaseStock(Long id);


    VoucherVo getById(Long id);
    SeckillVoucherVo queryKillById(Long id);
}

