package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.ShopTypeService;
import com.example.springbootredis.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * (ShopType)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/shopType")
public class ShopTypeController  {

    @Autowired
    private ShopTypeService shopTypeService;

    @GetMapping("/list")
    public JsonResponse<Object> list(){
        return ResponseUtils.success(shopTypeService.list());
    }

}

