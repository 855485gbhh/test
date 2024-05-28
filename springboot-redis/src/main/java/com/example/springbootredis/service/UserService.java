package com.example.springbootredis.service;


import com.example.springbootredis.pojo.dto.LoginDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import jakarta.servlet.http.HttpSession;

/**
 * (User)表服务接口
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
public interface UserService  {

    String captcha(String phone, HttpSession session);

    Object login(LoginDto loginDto, HttpSession session);

    JsonResponse<Object> report();

    JsonResponse<Object> statisticsReport();
    JsonResponse<Object> recentReportTimes();
}

