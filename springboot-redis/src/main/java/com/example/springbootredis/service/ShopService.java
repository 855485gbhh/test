package com.example.springbootredis.service;


import com.example.springbootredis.pojo.dto.ShopDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.ShopVo;

import java.util.List;

/**
 * (Shop)表服务接口
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
public interface ShopService {

    ShopVo getById(Long id);

    List<ShopVo> getAll();

    ShopVo update(ShopDto shopDto);

    ShopVo selectOne(Long id);

    JsonResponse<Object> ofType(Long shopTypeId, Integer pageNum, Integer pageSize, Double lon, Double lat);
}

