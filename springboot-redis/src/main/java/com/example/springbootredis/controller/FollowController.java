package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * (Follow)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/follow")
public class FollowController  {

    @Autowired
    private FollowService followService;

    /**
     *
     * @param id 关注或取关的用户id
     * @param isFollow 关注或取关行为
     * @return
     */
    @PutMapping("/{id}/{isFollow}")
    public JsonResponse<Object> follow(@PathVariable("id") Long id,@PathVariable("isFollow") Boolean isFollow){
        return followService.follow(id,isFollow);
    }

    @GetMapping("/common/{id}")
    public JsonResponse<Object> followCommons(@PathVariable("id") Long id){
        return followService.followCommons(id);
    }

}

