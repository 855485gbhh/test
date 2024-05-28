package com.example.springbootredis.service;


import com.example.springbootredis.pojo.dto.BlogDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.BlogVo;

import java.io.Serializable;

/**
 * (Blog)表服务接口
 *
 * @author qingzhou
 * @since 2024-05-04 10:48:46
 */
public interface BlogService  {

    BlogVo getById(Serializable id);
    JsonResponse<Object> queryById(Serializable id);
    JsonResponse<Object> like(Long id);


    JsonResponse<Object> list();

    JsonResponse<Object> queryBlogLikes(Long id);

    JsonResponse<Object> add(BlogDto blogDto);

    JsonResponse<Object> followed(Long time, Integer offSet);
}

