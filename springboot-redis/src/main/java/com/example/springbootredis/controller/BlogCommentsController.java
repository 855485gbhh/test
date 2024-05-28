package com.example.springbootredis.controller;

import com.example.springbootredis.service.BlogCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * (BlogComments)表控制层
 *
 * @author qingzhou
 * @since 2024-05-04 11:03:36
 */
@RestController
@RequestMapping("/blogComments")
public class BlogCommentsController  {

    @Autowired
    private BlogCommentsService blogCommentsService;
}

