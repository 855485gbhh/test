package com.example.springbootredis.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.example.springbootredis.pojo.dto.UserDto;
import com.example.springbootredis.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AllInterceptor implements HandlerInterceptor {
    private RedisTemplate<String,Object> redisTemplate;

    public AllInterceptor(RedisTemplate<String,Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session对象
//        HttpSession session = request.getSession();
//        //获取session中的用户对象
//        UserDto user = (UserDto) session.getAttribute("user");
//        //用户不存在，则拦截
//        if (user == null) {
//            response.setStatus(401);
//            return false;
//        }
//
//        //将用户信息保存到ThreadLocal
//        UserHolder.saveUser(user);
//        //放行
//        return true;
        //redis
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            return true;
        }

        Map<Object, Object> userMap = redisTemplate.opsForHash().entries("login:token:" + token);
        if (userMap.isEmpty()) {
            return false;
        }
        UserDto userDto = BeanUtil.fillBeanWithMap(userMap, new UserDto(), false);
        // 写入ThreadLocal
        UserHolder.saveUser(userDto);


        //刷新token有效期
        redisTemplate.expire("login:token:" + token, 30, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }

}
