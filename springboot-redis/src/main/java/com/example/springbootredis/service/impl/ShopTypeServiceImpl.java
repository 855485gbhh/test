package com.example.springbootredis.service.impl;
import com.example.springbootredis.pojo.vo.ShopTypeVo;
import com.example.springbootredis.utils.RedisUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.example.springbootredis.dao.ShopTypeMapper;
import com.example.springbootredis.service.ShopTypeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.springbootredis.pojo.po.table.ShopTypeTableDef.SHOP_TYPE;

/**
 * (ShopType)表服务实现类
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
@Service
public class ShopTypeServiceImpl  implements ShopTypeService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private ShopTypeMapper shopTypeMapper;
    @Override
    public List<ShopTypeVo> list() {
        List<ShopTypeVo> shopTypeVos = shopTypeMapper.selectListByQueryAs(QueryWrapper.create().from(SHOP_TYPE)
                        .orderBy(SHOP_TYPE.SORT,false),
                ShopTypeVo.class);
        RedisUtils.lSet("shop:type",shopTypeVos);
        return shopTypeVos;
    }
}

