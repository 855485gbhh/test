package com.example.springbootredis.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtils {
    private static RedisTemplate redisTemplate;
    public static final String LOCK_PREFIX = "redis:lock:";
    //不同的jvm分配不同的uuid
    public static final String ID_PREFIIX = UUID.randomUUID().toString(true) + "-";

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisLockUtils.redisTemplate = redisTemplate;
    }

    public static final DefaultRedisScript<Long> script;

    static {
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("unlock.lua"));
        script.setResultType(Long.class);
    }

    public static boolean tryLock(String lockSuffix, long seconds) {
        String threadId = ID_PREFIIX + Thread.currentThread().getId();
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + lockSuffix, threadId + "", seconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ifAbsent);
    }

    public static void unLock(String lockSuffix) {
        String threadId = ID_PREFIIX + Thread.currentThread().getId();
        redisTemplate.execute(script,
                Collections.singletonList(LOCK_PREFIX + lockSuffix),
                Collections.singletonList(threadId));
    }

//    public static boolean unLock(String lockSuffix) {
//        String threadId = ID_PREFIIX + Thread.currentThread().getId();
//        String cacheLockId = ((String) redisTemplate.opsForValue().get(LOCK_PREFIX + lockSuffix));
//        if (threadId.equals(cacheLockId)) {
//            Boolean delete = redisTemplate.delete(LOCK_PREFIX + lockSuffix);
//            return Boolean.TRUE.equals(delete);
//
//        }
//        return false;
//    }
}
