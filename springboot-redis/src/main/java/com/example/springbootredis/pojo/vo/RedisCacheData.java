package com.example.springbootredis.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisCacheData<T> {
    private T data;
    private LocalDateTime expire;

}
