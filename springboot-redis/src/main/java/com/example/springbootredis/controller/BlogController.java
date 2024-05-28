package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.dto.BlogDto;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.BlogService;
import com.example.springbootredis.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * (Blog)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:27
 */
@RestController
@RequestMapping("/blog")
public class BlogController  {

    @Autowired
    private BlogService blogService;

    @GetMapping("/{id}")
    public JsonResponse<Object> getById(@PathVariable Long id){
        return blogService.queryById(id);
    }
    @PutMapping("/like/{id}")
    public JsonResponse<Object> like(@PathVariable Long id){
        return blogService.like(id);
    }

    @GetMapping("/list")
    public JsonResponse<Object> list(){
        return blogService.list();
    }

    @GetMapping("/likes/{id}")
    public JsonResponse<Object> queryBlogLikes(@PathVariable Long id){
        return blogService.queryBlogLikes(id);
    }

    @PostMapping("/add")
    public JsonResponse<Object> add(@RequestBody BlogDto blogDto){
        return blogService.add(blogDto);
    }
    @GetMapping("/followed")
    public JsonResponse<Object> followed(@RequestParam Long time,@RequestParam Integer offSet){
        return blogService.followed(time,offSet);
    }
}

