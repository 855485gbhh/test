package com.example.springbootredis.utils;


import com.example.springbootredis.pojo.dto.UserDto;

public class UserHolder {
    private static final ThreadLocal<UserDto> tl = new ThreadLocal<>();

    public static void saveUser(UserDto userDto){
        tl.set(userDto);
    }

    public static UserDto getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
