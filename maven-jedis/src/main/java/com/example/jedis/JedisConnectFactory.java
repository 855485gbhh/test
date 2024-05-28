package com.example.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class JedisConnectFactory {
    private static final JedisPool jedisPool;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 6379;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接
        jedisPoolConfig.setMaxTotal(8);
        //最大空闲链接
        jedisPoolConfig.setMaxIdle(8);
        //最小空闲链接
        jedisPoolConfig.setMinIdle(0);
        //最长等待时长，ms
        jedisPoolConfig.setMaxWait(Duration.ofMillis(200L));
         jedisPool = new JedisPool(jedisPoolConfig, HOST, PORT, 1000);
    }

    //获取Jedis对象
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }
}
