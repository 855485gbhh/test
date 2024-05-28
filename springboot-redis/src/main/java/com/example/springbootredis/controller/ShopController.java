package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.dto.ShopDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.ShopService;
import com.example.springbootredis.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * (Shop)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/shop")
public class ShopController  {

    @Autowired
    private ShopService shopService;

    @GetMapping("/{id}")
    public JsonResponse<Object> getById(@PathVariable Long id){
        return ResponseUtils.success(shopService.getById(id));
    }
    @PutMapping("/update")
    public JsonResponse<Object> update(@RequestBody ShopDto shopDto){
        return ResponseUtils.success(shopService.update(shopDto));
    }

    @GetMapping("/oftype")
    public JsonResponse<Object> ofType(@RequestParam("typeId") Long shopTypeId,@RequestParam Integer pageNum,@RequestParam Double lon,@RequestParam Double lat){
        return shopService.ofType(shopTypeId,pageNum,10,lon,lat);
    }
}

