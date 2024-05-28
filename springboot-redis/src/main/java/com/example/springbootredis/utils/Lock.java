package com.example.springbootredis.utils;

public interface Lock {

     boolean tryLock(String lockPrefix,long seconds);

     boolean unLock(String lockSuffix);
}
