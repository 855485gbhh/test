package com.example.springbootredis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class IDUniqueGenerator {
    public static final long START_TIME = 1704067200;
    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        IDUniqueGenerator.redisTemplate = redisTemplate;
    }

    public long nextId(String keyPrefix) {
        //生成31位时间戳
        //获得当前时间的秒数
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - START_TIME;
        //生成32位序列号
        String day = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long increment = redisTemplate.opsForValue().increment(keyPrefix  + day);
        return timeStamp << 32|increment;

    }


}
