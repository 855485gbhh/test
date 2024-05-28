package com.example.springbootredis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.pojo.dto.ShopDto;
import com.example.springbootredis.pojo.po.Shop;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.RedisCacheData;
import com.example.springbootredis.pojo.vo.ShopVo;
import com.example.springbootredis.utils.RedisCacheUtils;
import com.example.springbootredis.utils.RedisUtils;
import com.example.springbootredis.utils.ResponseUtils;
import com.mybatisflex.core.query.QueryWrapper;

import com.example.springbootredis.dao.ShopMapper;
import com.example.springbootredis.service.ShopService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.example.springbootredis.constant.RedisConstant.*;
import static com.example.springbootredis.pojo.po.table.ShopTableDef.SHOP;

/**
 * (Shop)表服务实现类
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
@Slf4j
@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ShopMapper shopMapper;

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    @Resource
    private RedisCacheUtils redisCacheUtils;

    @Override
    public ShopVo getById(Long id) {
        return query(id);
    }

    @Override
    public List<ShopVo> getAll() {
       return   shopMapper.selectListByQueryAs(QueryWrapper.create().select().from(SHOP), ShopVo.class);
    }

    //互斥锁解决缓存击穿
    public ShopVo queryCache(Long id) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("redis:cache:shop:" + id);
        ShopVo shopVo = BeanUtil.fillBeanWithMap(entries, new ShopVo(), false);
        if (shopVo != null && shopVo.getId() != null) {
            return shopVo;
        }
        try {
            //添加互斥锁，true: 去数据库里查询数据，再写入redis
            //           false: 休眠一段时间，再次从redis中读取数据
            if (lock("redis:lock:shop" + id)) {
                shopVo = shopMapper.selectOneByQueryAs(QueryWrapper.create().from(SHOP)
                        .where(SHOP.ID.eq(id)), ShopVo.class);
                if (shopVo == null) {
                    redisTemplate.opsForHash().putAll("redis: cache:shop:" + id, null);
                    redisTemplate.expire("redis: cache:shop:" + id, 1, TimeUnit.MINUTES);
                    return null;
                }
                Map<String, Object> shopVoMap = BeanUtil.beanToMap(shopVo, new HashMap<>(), CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((name, value) -> value.toString()));
                redisTemplate.opsForHash().putAll("redis:cache:shop:" + id, shopVoMap);
                //释放锁
                delLock("redis:lock:shop" + id);
                return shopVo;
            } else {
                Thread.sleep(50);
                return queryCache(id);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //逻辑过期解决缓存击穿
    public ShopVo queryLogicExpire(Long id) {
        //先在redis里查询
        Object object = redisTemplate.opsForValue().get("redis:cache:shop:" + id);
        RedisCacheData cacheData = JSONObject.parseObject(JSONObject.toJSONString(object), RedisCacheData.class);
        ShopVo shopVo = JSONObject.parseObject(cacheData.getData().toString(), ShopVo.class);


        //redis不存在，直接返回null
        if (shopVo == null && shopVo.getId() == null) {
            return null;
        }
        //shopVo不为null
        //没过期直接返回
        LocalDateTime expire = cacheData.getExpire();
        if (expire.isAfter(LocalDateTime.now())) {
            return shopVo;
        }

        //尝试获取互斥锁，得不到直接返回shopVo
        if (!lock(REDIS_LOCK_SHOP + id)) {
            return shopVo;
        }
        //过期了也直接返回，拉取一个线程写入redis更新数据
        executor.submit(() -> {
                    try {
                        //重建缓存
                        preHeat(id, 10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        delLock(REDIS_LOCK_SHOP + id);
                    }
                }
        );
        return shopVo;
    }


    public void preHeat(Long id, long time, TimeUnit timeUnit) {
        ShopVo shopVo = selectOne(id);
        RedisCacheData cacheData = new RedisCacheData(shopVo, LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        String jsonStr = JSONObject.toJSONString(cacheData);
        redisTemplate.opsForValue().set(REDIS_CACHE_SHOP + id, jsonStr);

    }

    //缓存穿透
    public ShopVo query(Long id) {
        //先在redis里查询
        Map<Object, Object> shopMap = redisTemplate.opsForHash().entries("redis:cache:shop:" + id);
        ShopVo shopVo = BeanUtil.fillBeanWithMap(shopMap, new ShopVo(), false);

        //若存在则直接返回
        if (shopVo != null && shopVo.getId() != null) {
            log.info("从redis读取到数据：{}", shopVo);
            //      redisTemplate.expire("redis:cache:shop:" + id, 2, TimeUnit.MINUTES);
            return shopVo;
        }
        //不存在则到数据库里查
        shopVo = shopMapper.selectOneByQueryAs(QueryWrapper.create().from(SHOP)
                .where(SHOP.ID.eq(id)), ShopVo.class);
        //数据库里查不到,则id错误
        if (shopVo == null) {
            RedisUtils.set("redis:cache:shop:" + id, "", 1, TimeUnit.MINUTES);
            return null;
        }
        log.info("从数据库里查询到数据：{}", shopVo);
        Map<String, Object> map = BeanUtil.beanToMap(shopVo, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                        .setFieldValueEditor((name, value) -> {
                           if(value!=null){
                               value=value.toString();
                           }
                           return value;
                        }));
        //数据库里查到了，则写入到redis里面
        log.info("将数据写入redis");
        redisTemplate.opsForHash().putAll("redis:cache:shop:" + id, map);
        redisTemplate.expire("redis:cache:shop" + id, 2, TimeUnit.MINUTES);
        redisTemplate.opsForGeo().add(REDIS_GEO_SHOP_TYPE + shopVo.getTypeId(),
                new Point(Double.parseDouble(shopVo.getX()), Double.parseDouble(shopVo.getY())),
                shopVo.getId());
        return shopVo;
    }

    public ShopVo cacheThough(Long id) {
        return redisCacheUtils.cacheThough(REDIS_CACHE_SHOP, id, ShopVo.class, this::selectOne, 30, TimeUnit.SECONDS);
    }

    @Override
    public ShopVo update(ShopDto shopDto) {
        //先更新数据库
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopDto, shop);

        shopMapper.update(shop);
        //再删除redis缓存
        RedisUtils.del("redis:cache:shop" + shopDto.getId());
        return getById(shop.getId());
    }

    @Override
    public ShopVo selectOne(Long id) {
        return shopMapper.selectOneByQueryAs(QueryWrapper.create().from(SHOP)
                .where(SHOP.ID.eq(id)), ShopVo.class);
    }

    @Override
    public JsonResponse<Object> ofType(Long shopTypeId, Integer pageNum,Integer pageSize,Double lon, Double lat) {
        int from = (pageNum - 1) * pageSize;
        int end=pageNum*pageSize;

        GeoResults<RedisGeoCommands.GeoLocation<Object>> search = redisTemplate.opsForGeo().search(REDIS_GEO_SHOP_TYPE + shopTypeId, GeoReference.fromCoordinate(lon, lat),
                new Distance(1000, Metrics.METERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                        .limit(end)
                        .sortAscending()
                        .includeDistance()
        );

        List<ShopVo> shopVos=new ArrayList<>();
//        List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> contents = search.getContent();
//        contents.stream().forEach(geoLocation->{
//            Distance distance = geoLocation.getDistance();
//            System.out.println(distance.toString());
//            RedisGeoCommands.GeoLocation<Object> point = geoLocation.getContent();
////            Point  p= point.getPoint();
////            double x = p.getX();
////            double y = p.getY();
//            Object name = point.getName();
//            ShopVo shopVo = shopMapper.selectOneByQueryAs(QueryWrapper.create().select().from(SHOP).where(SHOP.ID.eq(name)), ShopVo.class);
//            shopVo.setDistance(distance.toString());
//            shopVos.add(shopVo);
//        });
        List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> resultList = search.getContent();
        if(resultList.size()<from){
            return ResponseUtils.success(Collections.emptyList());
        }
        resultList.stream().skip(from).forEach(result->{
            Object shopId = result.getContent().getName();
            String distance = result.getDistance().toString();
            ShopVo shopVo = shopMapper.selectOneByQueryAs(QueryWrapper.create().select().from(SHOP).where(SHOP.ID.eq(shopId)), ShopVo.class);
            shopVo.setDistance(distance);
            shopVos.add(shopVo);
        });
        return ResponseUtils.success(shopVos);
    }

    private boolean lock(String key) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);

    }

    private boolean delLock(String key) {
        Boolean delete = redisTemplate.delete(key);
        return Boolean.TRUE.equals(delete);
    }

    public Map<Long, List<ShopVo>> listByType() {
        List<ShopVo> shopVos = shopMapper.selectListByQueryAs(QueryWrapper.create().select().from(SHOP), ShopVo.class);
        Map<Long, List<ShopVo>> map = shopVos.stream().collect(Collectors.groupingBy(ShopVo::getTypeId));
        map.entrySet().stream().filter(entry -> entry.getKey() != null).forEach(entry -> {
            List<ShopVo> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<Object>> geoLocationList =
                    value.stream().map(shopVo ->
                            new RedisGeoCommands.GeoLocation<Object>(shopVo.getId(),
                                    new Point(Double.parseDouble(shopVo.getX()), Double.parseDouble(shopVo.getY())))).collect(Collectors.toList());
            redisTemplate.opsForGeo().add(REDIS_GEO_SHOP_TYPE + entry.getKey(), geoLocationList);

//                redisTemplate.opsForGeo().add(REDIS_GEO_SHOP_TYPE+entry.getKey(),
//                        new Point(Double.parseDouble(shopVo.getX()),Double.parseDouble(shopVo.getY())),shopVo.getId());

        });
        return map;
    }
}

