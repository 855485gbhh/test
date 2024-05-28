package com.example.springbootredis.controller;

import com.example.springbootredis.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * (UserInfo)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController  {

    @Autowired
    private UserInfoService userInfoService;
}

