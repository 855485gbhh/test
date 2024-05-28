package com.example.springbootredis.service;


import com.example.springbootredis.pojo.response.JsonResponse;

/**
 * (Follow)表服务接口
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:55
 */
public interface FollowService  {

    JsonResponse<Object> follow(Long id, Boolean isFollow);

    JsonResponse<Object> followCommons(Long id);
}

