package com.example.springbootredis.pojo.po;

import lombok.Data;

@Data
public class ValueScore<T> {
    private Long score;
    private T value;
}
