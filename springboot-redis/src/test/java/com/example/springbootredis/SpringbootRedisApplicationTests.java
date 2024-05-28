package com.example.springbootredis;

import cn.hutool.core.bean.BeanUtil;
import com.example.springbootredis.dao.SeckillVoucherMapper;
import com.example.springbootredis.dao.ShopMapper;
import com.example.springbootredis.pojo.po.Shop;
import com.example.springbootredis.pojo.po.User;
import com.example.springbootredis.pojo.po.ValueScore;
import com.example.springbootredis.pojo.vo.BlogVo;
import com.example.springbootredis.pojo.vo.SeckillVoucherVo;
import com.example.springbootredis.pojo.vo.ShopVo;
import com.example.springbootredis.pojo.vo.VoucherVo;
import com.example.springbootredis.service.ShopService;
import com.example.springbootredis.service.VoucherService;
import com.example.springbootredis.service.impl.ShopServiceImpl;
import com.example.springbootredis.utils.IDUniqueGenerator;
import com.example.springbootredis.utils.RedisUtils;
import com.example.springbootredis.utils.RegexUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryChain;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.springbootredis.constant.RedisConstant.REDIS_GEO_SHOP_TYPE;
import static com.example.springbootredis.constant.RedisConstant.REDIS_KILL_VOUCHER;
import static com.example.springbootredis.pojo.po.table.SeckillVoucherTableDef.SECKILL_VOUCHER;

@SpringBootTest
class SpringbootRedisApplicationTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private IDUniqueGenerator idUniqueGenerator;

    @Autowired
    private VoucherService voucherService;
    private static final ExecutorService es = Executors.newFixedThreadPool(500);
    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;
    @Resource
    private ShopMapper shopMapper;

    @Test
    void doing() throws Exception {
        SeckillVoucherVo seckillVoucherVo = voucherService.queryKillById(2L);
        System.out.println(seckillVoucherVo.toString());

    }

    @Test
    void str() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_KILL_VOUCHER + 2);
        SeckillVoucherVo seckillVoucherVo = BeanUtil.fillBeanWithMap(entries, new SeckillVoucherVo(), false);
        System.out.println(seckillVoucherVo.toString());
        ValueScore<BlogVo> valueScore = new ValueScore<>();
    }

    @Resource
    private ShopServiceImpl shopServiceImpl;

    @Test
    void stream() {
        Map<Long, List<ShopVo>> map = shopServiceImpl.listByType();
        map.entrySet().forEach(entry -> {
                    Long key = entry.getKey();
                    List<ShopVo> value = entry.getValue();
                    System.out.println("=====" + key + "=====");
                    value.stream().forEach(System.out::println);


                }
        );

    }

    @Test
    void pipe() {
        List<Integer> list = new ArrayList<>(100000);
        for (int i = 0; i < 1000000; i++) {
            list.add(i);
        }
        Map map = list.stream().collect(Collectors.groupingBy(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) {
                return "test:" + (integer % 100000);
            }
        }));
        RedisUtils.mset(map);
//        List<?> result = RedisUtils.pipelineForList(map);
//        result.forEach(System.out::println);

    }

    @Test
    void geo() {
        List<Serializable> ids = RedisUtils.pageSearch(REDIS_GEO_SHOP_TYPE + 1, new Point(121, 30), 100D, Metrics.KILOMETERS, 1, 10);
        List<Shop> shops = shopMapper.selectListByIds(ids);
        shops.forEach(System.out::println);
    }
}

























