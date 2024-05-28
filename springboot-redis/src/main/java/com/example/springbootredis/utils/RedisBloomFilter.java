package com.example.springbootredis.utils;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.pojo.po.BloomFilterProperties;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisBloomFilter {
    /**
     * 预期插入量
     */
    private static Long expectedInsertions = 1000L;
    /**
     * 误判率（大于0，小于1.0）
     */
    private static Double fpp = 0.03D;
    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 二进制位大小（多少位）
     */
    private Long size;
    /**
     * hash函数个数
     */
    private Integer numHashFunctions;
    /**
     * guava实现
     */
    private Funnel<CharSequence> funnel = Funnels.stringFunnel(Charset.forName("UTF-8"));

    @PostConstruct
    public void initRedisBloomFilter() {
        this.size = optimalNumOfBits(expectedInsertions, fpp);
        this.numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, size);
    }

    /**
     * <p>
     * 最佳的位数（二进制位数）
     * </p>
     * <p>时间：2021年2月7日-上午10:13:43</p>
     *
     * @param expectedInsertions 预期插入的数量
     * @param fpp                错误率大于0，小于1.0
     * @return long
     * @author xg
     */
    public long optimalNumOfBits(long expectedInsertions, double fpp) {
        if (fpp == 0) {
            fpp = 0.03D;
        }
        return (long) (-expectedInsertions * Math.log(fpp) / (Math.log(2) * Math.log(2)));
    }

    /**
     * <p>
     * 最佳的Hash函数个数
     * </p>
     * <p>时间：2021年2月7日-上午10:17:26</p>
     *
     * @param expectedInsertions 预期插入的数量
     * @param numBits            根据optimalNumOfBits方法计算的最佳二进制位数
     * @return int
     * @author xg
     */
    public static int optimalNumOfHashFunctions(long expectedInsertions, long numBits) {
        return Math.max(1, (int) Math.round((double) numBits / expectedInsertions * Math.log(2)));
    }

    // 存数据
//    public boolean put(String key, String value) {
//        byte[] bytes = Hashing.murmur3_128().hashObject(value, funnel).asBytes();
//        long hash1 = lowerEight(bytes);
//        long hash2 = upperEight(bytes);
//
//        boolean bitsChanged = false;
//        long combinedHash = hash1;
//        for (int i = 0; i < numHashFunctions; i++) {
//            long bit = (combinedHash & Long.MAX_VALUE) % numBits;
//            // 这里设置对应的bit为1
//            redisTemplate.opsForValue().setBit(key, bit, true);
//            combinedHash += hash2;
//        }
//        return bitsChanged;
//    }

    // 判断数据是否已经存在
//    public boolean isExist(String key, String value) {
//        byte[] bytes = Hashing.murmur3_128().hashObject(value, funnel).asBytes();
//        long hash1 = lowerEight(bytes);
//        long hash2 = upperEight(bytes);
//
//        long combinedHash = hash1;
//        for (int i = 0; i < numHashFunctions; i++) {
//            long bit = (combinedHash & Long.MAX_VALUE) % numBits;
//            // 这里判断redis中对应位是否为1
//            if (!redisTemplate.opsForValue().getBit(key, bit)) {
//                return false;
//            }
//            combinedHash += hash2;
//        }
//        return true;
//    }

    private long lowerEight(byte[] bytes) {
        return Longs.fromBytes(bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    }

    private long upperEight(byte[] bytes) {
        return Longs.fromBytes(bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * redisson实现
     */

    private static RedissonClient redissonClient;

    @Autowired
    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public static void put(String name, String key, long time, TimeUnit timeUnit) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, fpp);
        bloomFilter.expire(time, timeUnit);
        bloomFilter.add(key);
    }

    public static void put(String name, String key) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, fpp);
        bloomFilter.add(key);
    }

    public static void put(String name, List<String> keys, long time, TimeUnit timeUnit) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, fpp);
        bloomFilter.expire(time, timeUnit);
        keys.forEach(key -> bloomFilter.add(key));
    }

    public static void put(String name, List<String> keys) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, fpp);
        keys.forEach(key -> bloomFilter.add(key));
    }

    public static void put(String name, long time, TimeUnit timeUnit, String... keys) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, fpp);
        bloomFilter.expire(time, timeUnit);
        Arrays.stream(keys).forEach(key -> bloomFilter.add(key));
    }

    public static void put(String name, String... keys) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, fpp);
        Arrays.stream(keys).forEach(key -> bloomFilter.add(key));
    }

    /**
     * 存入的元素个数
     *
     * @param name
     * @return
     */
    public static long count(String name) {
        return redissonClient.getBloomFilter(name).count();
    }

    public static boolean isExist(String name) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        return bloomFilter.isExists();
    }

    public static boolean isExist(String name, String key) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, fpp);
        return bloomFilter.contains(key);
    }

    public static String info(String name) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(name);
        //预期插入的数量
        long currentExpectedInsertions = bloomFilter.getExpectedInsertions();
        //二进制数组长度
        long currentSize = bloomFilter.getSize();
        //存入的元素个数
        long currentCount = bloomFilter.count();
        //误判率
        double currentFpp = bloomFilter.getFalseProbability();
        //每个元素使用的哈希迭代次数
        int currentHashIterations = bloomFilter.getHashIterations();
        //
        long timeToLive = bloomFilter.remainTimeToLive();
        //返回当前对象上一次修改或读取的空闲时间
        Long currentIdleTime = bloomFilter.getIdleTime();
        JSONObject object = JSONObject.of("expectedInsertions", currentExpectedInsertions);
        object.put("fpp", currentFpp);
        object.put("count", currentCount);
        object.put("size", currentSize);
        object.put("hashIterations", currentHashIterations);
        object.put("timeToLive",timeToLive);
        object.put("idleTime", currentIdleTime);
        return object.toString();
    }

}
