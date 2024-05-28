package com.example.springbootredis.service;


import com.example.springbootredis.pojo.dto.OrderDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.OrderVo;

/**
 * (Order)表服务接口
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
public interface VoucherOrderService  {

    JsonResponse<Object> kill(Long id);

    OrderVo getById(Long id);

    JsonResponse<Object> queryById(Long id);

    JsonResponse<Object> generate(Long id, Long userId);
    OrderVo insertOrder(OrderDto orderDto);
}

