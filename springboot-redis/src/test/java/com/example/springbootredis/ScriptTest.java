package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

@SpringBootTest
public class ScriptTest {
    public static final DefaultRedisScript<Long> script;
    @Autowired
    private StringRedisTemplate redisTemplate;

    static {
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("test.lua"));
        script.setResultType(Long.class);
    }
    @Test
    void test(){
        Long execute = redisTemplate.execute(script,  Collections.singletonList(2+""),6+"");
        System.out.println("execute = " + execute);
        
    }

}
