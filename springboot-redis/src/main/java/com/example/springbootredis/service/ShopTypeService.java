package com.example.springbootredis.service;




import com.example.springbootredis.pojo.vo.ShopTypeVo;

import java.util.List;

/**
 * (ShopType)表服务接口
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
public interface ShopTypeService  {

    List<ShopTypeVo> list();
}

