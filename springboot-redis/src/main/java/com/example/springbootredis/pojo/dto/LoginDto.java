package com.example.springbootredis.pojo.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String phone;
    private String password;
    private String code;
}
