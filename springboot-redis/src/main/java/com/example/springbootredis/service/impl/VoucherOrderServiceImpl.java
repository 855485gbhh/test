package com.example.springbootredis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.pojo.dto.OrderDto;
import com.example.springbootredis.pojo.po.Order;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.OrderVo;
import com.example.springbootredis.pojo.vo.SeckillVoucherVo;
import com.example.springbootredis.pojo.vo.VoucherVo;
import com.example.springbootredis.service.VoucherService;
import com.example.springbootredis.utils.IDUniqueGenerator;

import com.example.springbootredis.utils.RedisLockUtils;
import com.example.springbootredis.utils.ResponseUtils;
import com.example.springbootredis.utils.UserHolder;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;

import com.example.springbootredis.dao.VoucherOrderMapper;
import com.example.springbootredis.service.VoucherOrderService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.example.springbootredis.constant.RedisConstant.*;
import static com.example.springbootredis.pojo.po.table.OrderTableDef.ORDER;


/**
 * (Order)表服务实现类
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl implements VoucherOrderService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private VoucherService voucherService;
    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Autowired
    private IDUniqueGenerator idUniqueGenerator;
    @Autowired
    private RedissonClient redissonClient;

//    @PostConstruct
//    public void init() {
//        ORDER_EXECUTORS.submit(new OrderGenerate());
//    }

    private VoucherOrderService proxy;
    public static final DefaultRedisScript<Long> script;

    static {
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("seckill.lua"));
        script.setResultType(Long.class);
    }


    //private static final ExecutorService ORDER_EXECUTORS = Executors.newFixedThreadPool(10);

    //  private BlockingQueue<OrderDto> orderQueue = new ArrayBlockingQueue<>(1024);

//    private class OrderGenerate implements Runnable {
//
//        @Override
//        public void run() {
//            while (true) {
//                //阻塞获取
////                try {
////                    OrderDto order = orderQueue.take();
////                    insert(order);
////                } catch (InterruptedException e) {
////                    throw new RuntimeException(e);
////                }
//                //从消息队列中读取消息 XREADGROUP GROUP ordergroup consumer1 COUNT 1 BLOCK 2000 STREAMS streams.order >
//                //从ordergroup组里consumer1为消费者
//                //从streams.order里，每次读取一条消息，阻塞时间为2000ms
//                try {
//                    List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(Consumer.from("ordergroup", "consumer1"),
//                            StreamReadOptions.empty().count(1)
//                                    .block(Duration.ofSeconds(2)),
//                            StreamOffset.create("stream.order", ReadOffset.lastConsumed()));
//                    if (records == null || records.size() == 0) {
//                        continue;
//                    }
//                    //解析消息
//                    MapRecord<String, Object, Object> record = records.get(0);
//                    Map<Object, Object> recordValueMap = record.getValue();
//                    OrderDto order = BeanUtil.fillBeanWithMap(recordValueMap, new OrderDto(), false);
//                    //生成订单
//                    log.info("生成订单，插入数据库");
//                    insertOrder(order);
//                    //ACK确认消息
//                    redisTemplate.opsForStream().
//                            acknowledge("stream.order",
//                                    "ordergroup",
//                                    record.getId());
//                } catch (Exception e) {
//                    log.error("处理订单异常：{}", e);
//                    orderPendingList();
//                }
//
//            }
//        }
//    }

//    private void orderPendingList() {
//        int retryTimes = 0;
//        try {
//            while (retryTimes < 3) {
//                retryTimes++;
//                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
//                        .read(Consumer.from("ordergroup", "consumer1"),
//                                StreamReadOptions.empty().count(1),
//                                StreamOffset.create("stream.order", ReadOffset.from("0")));
//                //判断消息是否获取成功
//                if (records == null | records.size() == 0) {
//                    //如果获取失败，说明pending-list没有异常消息，结束循环
//                    break;
//                }
//                MapRecord<String, Object, Object> record = records.get(0);
//                Map<Object, Object> recordValueMap = record.getValue();
//                OrderDto orderDto = BeanUtil.fillBeanWithMap(recordValueMap, new OrderDto(), false);
//                insertOrder(orderDto);
//                //消息确认
//                redisTemplate.opsForStream().acknowledge("stream.order", "ordergroup", record.getId());
//            }
//        } catch (Exception e) {
//            log.error("处理pending-list订单异常：{}", e);
//        }
//    }


    @Override
    public JsonResponse<Object> kill(Long id) {
        return redissonLock(id);
    }

    public JsonResponse<Object> redissonLock(Long id) {
        SeckillVoucherVo seckillVoucherVo = voucherService.queryKillById(id);
        if (seckillVoucherVo == null) {
            return ResponseUtils.error(1, "该优惠券不是秒杀券");
        }
        if (seckillVoucherVo.getBeginTime().isAfter(LocalDateTime.now())) {
            return ResponseUtils.error(1, "秒杀活动尚未开始");
        }
        if (seckillVoucherVo.getEndTime().isBefore(LocalDateTime.now())) {
            return ResponseUtils.error(1, "秒杀活动已结束");
        }
        Long userId = UserHolder.getUser().getId();
        Long orderId = idUniqueGenerator.nextId(REDIS_ORDER);
        log.info("开始校验用户购买资格，当前用户：{}", userId);
        Long result = redisTemplate.execute(script,
                Collections.singletonList(id.toString()), userId.toString(), orderId.toString());

        if (result == 1) {
            return ResponseUtils.error(1, "秒杀券已抢空");
        }
        if (result == 2) {
            return ResponseUtils.error(1, "不能重复下单");
        }
        log.info("剩余秒杀券库存:{}", result);
        log.info("生成订单id：{}", orderId);
        //      RLock lock = redissonClient.getLock("redis:lock:killvoucher:stock:" + id);
        //生成订单
//        OrderDto order = new OrderDto();
//        order.setId(orderId);
//        order.setUserId(userId);
//        orderQueue.add(order);
//        //拿到代理对象
//        proxy = (VoucherOrderService) AopContext.currentProxy();
        return ResponseUtils.success(orderId);


    }


    public JsonResponse<Object> sy(Long id) {
        //获取秒杀券信息
        VoucherVo voucherVo = voucherService.getById(id);
        SeckillVoucherVo seckillVoucherVo = voucherVo.getSeckillVoucherVo();
        if (seckillVoucherVo == null) {
            return ResponseUtils.error(1, "该代金券不是秒杀券");
        }
        //秒杀是否开始
        if (seckillVoucherVo.getBeginTime().isAfter(LocalDateTime.now())) {
            return ResponseUtils.error(1, "秒杀活动尚未开始");
        }
        //判断秒杀是否结束
        if (seckillVoucherVo.getEndTime().isBefore(LocalDateTime.now())) {
            return ResponseUtils.error(1, "秒杀活动已结束");
        }
        // 秒杀活动
        Integer stock = seckillVoucherVo.getStock();
        if (stock <= 0) {
            return ResponseUtils.error(1, "秒杀券已抢空");
        }
        log.info("开始秒杀");
        Long userId = UserHolder.getUser().getId();
        synchronized (userId.toString().intern()) {
            VoucherOrderService proxy = ((VoucherOrderService) AopContext.currentProxy());
            return proxy.generate(id, userId);
        }
    }

    //集群模式下，使用redis分布式锁，防止一个用户购买多单
    public JsonResponse<Object> redisLock(Long id) {
        //获取秒杀券信息
        VoucherVo voucherVo = voucherService.getById(id);
        SeckillVoucherVo seckillVoucherVo = voucherVo.getSeckillVoucherVo();
        if (seckillVoucherVo == null) {
            return ResponseUtils.error(1, "该代金券不是秒杀券");
        }
        //秒杀是否开始
        if (seckillVoucherVo.getBeginTime().isAfter(LocalDateTime.now())) {
            return ResponseUtils.error(1, "秒杀活动尚未开始");
        }
        //判断秒杀是否结束
        if (seckillVoucherVo.getEndTime().isBefore(LocalDateTime.now())) {
            return ResponseUtils.error(1, "秒杀活动已结束");
        }
        // 秒杀活动
        Integer stock = seckillVoucherVo.getStock();
        if (stock <= 0) {
            return ResponseUtils.error(1, "秒杀券已抢空");
        }

        Long userId = UserHolder.getUser().getId();
        if (!RedisLockUtils.tryLock("order:" + id, 5L)) {
            //获取锁失败，返回错误或重试
            return ResponseUtils.error(1, "请勿频繁操作");
        }
        log.info("开始秒杀");
        try {
            VoucherOrderService proxy = ((VoucherOrderService) AopContext.currentProxy());
            return proxy.generate(id, userId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            RedisLockUtils.unLock("order:" + id);
        }
    }

    @Transactional
    public JsonResponse<Object> generate(Long id, Long userId) {

        long count = QueryChain.of(voucherOrderMapper).select()
                .from(ORDER)
                .where(ORDER.VOUCHER_ID.eq(id))
                .and(ORDER.USER_ID.eq(userId))
                .count();
        System.out.println(userId + ":" + count);
        if (count > 0) {
            return ResponseUtils.error(1, "不能重复下单");
        }
        boolean decreaseStock = voucherService.decreaseStock(id);
        if (!decreaseStock) {
            return ResponseUtils.error(1, "扣减秒杀券库存失败");
        }
//        seckillVoucherVo.setStock(seckillVoucherVo.getStock() - 1);
//        log.info("使用秒杀券成功,当前剩余：{}", seckillVoucherVo.getStock() - 1);
//        voucherVo.setSeckillVoucherVo(seckillVoucherVo);
//        redisTemplate.opsForValue().set(REDIS_KILL_VOUCHER + id, JSONObject.toJSONString(voucherVo));


        //生成订单
        long orderId = idUniqueGenerator.nextId(REDIS_ORDER);
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(id);
        voucherOrderMapper.insertSelectiveWithPk(order);
        //返回订单vo
        return queryById(orderId);
    }

    @Override
    public OrderVo insertOrder(OrderDto orderDto) {
        Order order = new Order();
        BeanUtils.copyProperties(orderDto, order);
        voucherOrderMapper.insertSelectiveWithPk(order);
        return getById(order.getId());

    }

    @Override
    public OrderVo getById(Long id) {
        return voucherOrderMapper.selectOneByQueryAs(QueryWrapper.create().from(ORDER)
                .where(ORDER.ID.eq(id)), OrderVo.class);
    }

    @Override
    public JsonResponse<Object> queryById(Long id) {
        String day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String str = (String) redisTemplate.opsForValue().get(REDIS_ORDER + day + ":" + id);
        OrderVo orderVo = JSONObject.parseObject(str, OrderVo.class);
        if (orderVo != null) {
            return ResponseUtils.success(orderVo);
        }
        orderVo = getById(id);
        if (orderVo == null) {
            redisTemplate.opsForValue().set(REDIS_ORDER + day + ":" + id, "", 5, TimeUnit.SECONDS);
            return ResponseUtils.error(1, "不存在的数据");
        }
        redisTemplate.opsForValue().set(REDIS_ORDER + day + ":" + id, JSONObject.toJSONString(orderVo), 30, TimeUnit.MINUTES);
        return ResponseUtils.success(orderVo);
    }
}

