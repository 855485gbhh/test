package com.example.springbootredis.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.dao.SeckillVoucherMapper;
import com.example.springbootredis.pojo.dto.VoucherDto;
import com.example.springbootredis.pojo.po.SeckillVoucher;
import com.example.springbootredis.pojo.po.Voucher;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.SeckillVoucherVo;
import com.example.springbootredis.pojo.vo.VoucherVo;
import com.example.springbootredis.utils.ResponseUtils;
import com.example.springbootredis.utils.UserHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;

import com.example.springbootredis.dao.VoucherMapper;
import com.example.springbootredis.service.VoucherService;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.springbootredis.constant.RedisConstant.*;
import static com.example.springbootredis.pojo.po.table.SeckillVoucherTableDef.SECKILL_VOUCHER;
import static com.example.springbootredis.pojo.po.table.VoucherTableDef.VOUCHER;

/**
 * (Voucher)表服务实现类
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
@Slf4j
@Service
public class VoucherServiceImpl implements VoucherService {
    @Resource
    private VoucherMapper voucherMapper;
    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final ExecutorService executors = Executors.newFixedThreadPool(10);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse<Object> addKill(VoucherDto voucherDto) {
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherDto, voucher);
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        BeanUtils.copyProperties(voucherDto, seckillVoucher);
        long id = 2L;
        voucher.setId(id);
        LocalDateTime now = LocalDateTime.now();
        voucher.setCreateTime(now);
        voucher.setUpdateTime(now);
        seckillVoucher.setCreateTime(now);
        seckillVoucher.setUpdateTime(now);
        seckillVoucher.setVoucherId(id);
        seckillVoucher.setBeginTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        seckillVoucher.setEndTime(LocalDateTime.of(2024, 12, 30, 12, 0));

        try {
            voucherMapper.insert(voucher);
            seckillVoucherMapper.insert(seckillVoucher);
            return ResponseUtils.success();
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseUtils.error(1, "秒杀卷添加失败");
        }
    }

    @Override
    public JsonResponse<Object> query(Long id) {
        VoucherVo voucherVo = queryById(id);
        if (voucherVo == null) {
            return ResponseUtils.error(1, "不存在的数据");
        }
        return ResponseUtils.success(voucherVo);
    }

    @Override
    public VoucherVo getById(Long id) {
        List<VoucherVo> voucherVos = voucherMapper.selectListByQueryAs(QueryWrapper.create().from(VOUCHER).select()
                        .where(VOUCHER.ID.eq(id)), VoucherVo.class,
                c -> c.field(VoucherVo::getSeckillVoucherVo).prevent().queryWrapper(seckillVoucherVo -> QueryWrapper.create()
                        .select()
                        .from(SECKILL_VOUCHER)
                        .where(SECKILL_VOUCHER.VOUCHER_ID.eq(id))));
        if (voucherVos == null || voucherVos.size() == 0) {
            return null;
        }
        return voucherVos.get(0);
    }

    @Override
    public SeckillVoucherVo queryKillById(Long id) {
        Map entries = redisTemplate.opsForHash().entries(REDIS_KILL_VOUCHER + id);
        SeckillVoucherVo seckillVoucherVo = BeanUtil.fillBeanWithMap(entries, new SeckillVoucherVo(), false);
        if (seckillVoucherVo != null && seckillVoucherVo.getVoucherId() != null) {
            log.info("从redis读取到：{}",seckillVoucherVo.getVoucherId());
            return seckillVoucherVo;
        }
        seckillVoucherVo = QueryChain.of(seckillVoucherMapper)
                .select()
                .from(SECKILL_VOUCHER)
                .where(SECKILL_VOUCHER.VOUCHER_ID.eq(id))
                .oneAs(SeckillVoucherVo.class);
        if (seckillVoucherVo == null) {
            redisTemplate.opsForHash().putAll(REDIS_KILL_VOUCHER + id, null);
            redisTemplate.expire(REDIS_KILL_VOUCHER + id, 5, TimeUnit.SECONDS);
            return null;
        }
        log.info("写入redis");
        redisTemplate.opsForHash().putAll(REDIS_KILL_VOUCHER + id, BeanUtil.beanToMap(seckillVoucherVo));
        redisTemplate.expire(REDIS_KILL_VOUCHER + id, 30, TimeUnit.MINUTES);
        return seckillVoucherVo;

    }

    @Override
    public VoucherVo queryById(Long id) {
        try {
            Map voucherMap = redisTemplate.opsForHash().entries(REDIS_KILL_VOUCHER + id);
            VoucherVo voucherVo = BeanUtil.fillBeanWithMap(voucherMap, new VoucherVo(), false);
            if (voucherVo != null && voucherVo.getId() != null) {
                log.info("从redis读取到：{}", voucherVo.getId());
                return voucherVo;
            }
            voucherVo = getById(id);
            if (voucherVo == null) {
                // redisTemplate.opsForValue().set(REDIS_KILL_VOUCHER + id, "", 10, TimeUnit.SECONDS);
                return null;
            }
//            String jsonString = JSONObject.toJSONString(voucherVo);
//            SeckillVoucherVo seckillVoucherVo = voucherVo.getSeckillVoucherVo();
            //将秒杀券库存写入redis
            log.info("将{}写入到redis", voucherVo.getId());
            Map<String, Object> map = BeanUtil.beanToMap(voucherVo.getSeckillVoucherVo());
            map.entrySet().forEach(System.err::println);
            redisTemplate.opsForHash().putAll(REDIS_KILL_VOUCHER + id, map);
            redisTemplate.expire(REDIS_KILL_VOUCHER + id, 30, TimeUnit.MINUTES);
            //  redisTemplate.opsForValue().set(REDIS_KILL_VOUCHER + id, seckillVoucherVo.getStock(), 30, TimeUnit.MINUTES);
            log.info("从mysql读取到：{}", voucherVo.getId());
            return voucherVo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean decreaseStock(Long id) {
        return UpdateChain.of(seckillVoucherMapper)

                .setRaw(SECKILL_VOUCHER.STOCK, "stock-1")
                .where(SECKILL_VOUCHER.VOUCHER_ID.eq(id))
                .and(SECKILL_VOUCHER.STOCK.gt(0))
                .update();

    }
}

