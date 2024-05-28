package com.example.springbootredis;

import com.example.springbootredis.utils.RedisBloomFilter;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @Test
    void lock() {
        try {
            //获取锁，可重入，指定锁的名称
            RLock lock = redissonClient.getLock("redis:lock:order");
            //尝试获取锁，锁的最大等待时间（期间会重试），锁自动释放时间，时间单位
            boolean isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (isLock) {
                System.out.println();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void bloomFilter() {
        RedisBloomFilter.put("sss",2,TimeUnit.MINUTES,"a","b","c","d","e");
    }

    @Test
    void count() {
        long count = RedisBloomFilter.count("test");
        System.out.println("count = " + count);
    }
    @Test
    void info() {
        System.out.println(RedisBloomFilter.info("test").toString());
    }
    @Test
    void exist(){
        System.out.println(RedisBloomFilter.isExist("test2"));
    }
}
