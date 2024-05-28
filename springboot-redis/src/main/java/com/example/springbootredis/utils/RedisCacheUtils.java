package com.example.springbootredis.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.pojo.vo.RedisCacheData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class RedisCacheUtils {

    private static RedisTemplate redisTemplate;
    private static final ExecutorService EXECUTORS = Executors.newFixedThreadPool(10);

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisCacheUtils.redisTemplate = redisTemplate;
    }

    public void set(String key, Object object, long time, TimeUnit timeUnit) {
        String jsonStr = JSONObject.toJSONString(object);
        redisTemplate.opsForValue().set(key, jsonStr, time, timeUnit);
    }

    public void preHeat(String keyPrefix, Long id, Object object, long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        RedisCacheData cacheData = new RedisCacheData(object, LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        String jsonStr = JSONObject.toJSONString(cacheData);
        redisTemplate.opsForValue().set(key, jsonStr);
    }

    public <R> void preHeat(String keyPrefix, Long id, Function<Long, R> db, long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        R r = db.apply(id);
        RedisCacheData cacheData = new RedisCacheData(r, LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        String jsonStr = JSONObject.toJSONString(cacheData);
        redisTemplate.opsForValue().set(key, jsonStr);
    }


    public <R> R cacheLogicExpire(String keyPrefix, Long id, Class<R> clazz, Function<Long, R> dbFallback, long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String str = (String) redisTemplate.opsForValue().get(key);
        RedisCacheData cacheData = JSONObject.parseObject(str, RedisCacheData.class);
        R r = JSONObject.parseObject(cacheData.getData().toString(), clazz);
        if (r == null) {
            return null;
        }
        LocalDateTime expire = cacheData.getExpire();
        if (expire.isAfter(LocalDateTime.now())) {
            return r;
        }
        EXECUTORS.submit(() -> {
            R apply = dbFallback.apply(id);
            this.preHeat(key, id, dbFallback, time, timeUnit);

        });
        return r;
    }

    public <R, ID> R cacheThough(String keyPrefix, ID id, Class<R> clazz, Function<ID, R> dbFallback, long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String json = redisTemplate.opsForValue().get(key).toString();
        // 2.判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3.存在，直接返回
            return JSONUtil.toBean(json, clazz);
        }
        // 判断命中的是否是空值
        if (json != null) {
            // 返回一个错误信息
            return null;
        }

        // 4.不存在，根据id查询数据库
        R r = dbFallback.apply(id);
        // 5.不存在，返回错误
        if (r == null) {
            // 将空值写入redis
            redisTemplate.opsForValue().set(key, "", 1, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 6.存在，写入redis
        this.set(key, r, time, timeUnit);
        return r;

    }
}
