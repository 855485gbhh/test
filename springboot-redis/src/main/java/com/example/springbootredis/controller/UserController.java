package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.dto.LoginDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.UserService;
import com.example.springbootredis.utils.ResponseUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * (User)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/user")

public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/captcha")
    public JsonResponse<Object> captcha(@RequestParam("phone") String phone, HttpSession session) {
        return ResponseUtils.success(userService.captcha(phone, session));
    }

    @PostMapping("/login")
    public JsonResponse<Object> login(@RequestBody LoginDto loginDto, HttpSession session) {
        return ResponseUtils.success(userService.login(loginDto,session));
    }

    @PostMapping("/report")
    public JsonResponse<Object>  report(){
        return userService.report();
    }

    @GetMapping("/statreport")
    public JsonResponse<Object> statisticsReport(){
        return userService.statisticsReport();
    }
}

