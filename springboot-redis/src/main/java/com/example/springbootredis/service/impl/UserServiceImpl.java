package com.example.springbootredis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.example.springbootredis.pojo.dto.LoginDto;
import com.example.springbootredis.pojo.dto.UserDto;
import com.example.springbootredis.pojo.po.User;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.ReportVo;
import com.example.springbootredis.utils.RedisUtils;
import com.example.springbootredis.utils.RegexUtils;
import com.example.springbootredis.utils.ResponseUtils;
import com.example.springbootredis.utils.UserHolder;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.example.springbootredis.dao.UserMapper;
import com.example.springbootredis.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.springbootredis.pojo.po.table.UserTableDef.USER;
import static com.example.springbootredis.constant.RedisConstant.*;

/**
 * (User)表服务实现类
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String captcha(String phone, HttpSession session) {
        log.info("开始校验手机号：{}", phone);
        //校验手机号
//        if (RegexUtils.isPhoneInvalid(phone) == false) {
//            return "手机号格式错误";
//        }
        //生成六位数验证码
        String code = RandomUtil.randomNumbers(6);
        //将验证码放入redis
        redisTemplate.opsForValue().set("login:code:" + phone, code, 30, TimeUnit.MINUTES);
        log.info("发送短信验证码：{}成功", code);
        return code;
    }

    @Override
    public Object login(LoginDto loginDto, HttpSession session) {
        //校验手机号
//        if (!RegexUtils.isPhoneInvalid(loginDto.getPhone())) {
//            return ResponseUtils.error(1,"手机号格式错误");
//        }
        //查看验证码是否一致
        Object cacheCode = redisTemplate.opsForValue().get("login:code:" + loginDto.getPhone());

        System.out.println("cacheCode = " + cacheCode);
        if (cacheCode == null || !cacheCode.equals(loginDto.getCode())) {
            return "验证码错误";
        }
        //查询出该手机号的用户
        QueryWrapper queryWrapper = QueryWrapper.create().from(USER)
                .where(USER.PHONE.eq(loginDto.getPhone()));
        User user = userMapper.selectOneByQueryAs(queryWrapper, User.class);
        //用户不存在则创建
        if (user == null) {
            user = createUser(loginDto.getPhone());
            userMapper.insertSelectiveWithPk(user);
        }
        //随机生成token，作为校验令牌
        String token = UUID.randomUUID().toString(true);
        //用户存在则保存用户信息到redis
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDto, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        redisTemplate.opsForHash().putAll("login:token:" + token, userMap);
        redisTemplate.expire("login:token:" + token, 600, TimeUnit.MINUTES);
        //返回token
        return token;

    }

    @Override
    public JsonResponse<Object> report() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        String REPORT_SUFFIX = userId + ":" + year + ":" + month;
        Boolean isReport = redisTemplate.opsForValue().getBit(REDIS_CACHE_USER_REPORT + REPORT_SUFFIX, day - 1);
        if (Boolean.TRUE.equals(isReport)) {
            return ResponseUtils.error(1, "请勿重复签到");
        }
        try {
            redisTemplate.opsForValue().setBit(REDIS_CACHE_USER_REPORT + REPORT_SUFFIX, day - 1, true);
            log.info("用户：{}签到成功", userId);
            return ResponseUtils.success("签到成功");
        } catch (Exception e) {
            log.error("签到失败：", e);
        }
        return ResponseUtils.error(2, "签到失败");
    }

    @Override
    public JsonResponse<Object> statisticsReport() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        String REPORT_SUFFIX = userId + ":" + year + ":" + month;
        long total = RedisUtils.bitCount(REDIS_CACHE_USER_REPORT + REPORT_SUFFIX,
                0, 100);
        int recent = (int) recentReportTimes().getData();
        return ResponseUtils.success(new ReportVo((int)total,recent,null));
    }

    @Override
    public JsonResponse<Object> recentReportTimes() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        String REPORT_SUFFIX = userId + ":" + year + ":" + month;
        //返回值可能有多个，如果有多个命令
        List<Long> bitResult = redisTemplate.opsForValue().bitField(REDIS_CACHE_USER_REPORT + REPORT_SUFFIX,
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(day)).valueAt(0));
        if (bitResult == null || bitResult.size() == 0) {
            return ResponseUtils.success(0);
        }
        Long result = bitResult.get(0);
        if (result == null || result == 0) {
            return ResponseUtils.success(0);
        }
        //统计最后一次登录的连续签到天数
        int count = 0;
        boolean isLastReport = false;
        while (true) {
            if ((result & 1) != 0) {
                count++;
                isLastReport = true;
            }
            if ((result & 1) == 0 && isLastReport == false) {
                continue;
            }
            if ((result & 1) == 0 && isLastReport == true) {
                break;
            }
            result >>>= 1;
        }
        return ResponseUtils.success(count);

    }

    private User createUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(8));
        return user;
    }
}

