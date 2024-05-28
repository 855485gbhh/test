package com.example.springbootredis.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

/**
 * 初始化对象工具类
 *
 * @param <T>
 */
@Slf4j
@Component
public class PojoUtils<T> {

    /**
     * 初始化对象
     * <p>
     * 初始化属性：id，createTime， updateTime
     *
     * @param entity
     * @return
     */
    public T initPojo(T entity) {
        try {
            //1、获取对象的类型
            Class<?> clazz = entity.getClass();

            //2、获取属性名为id的属性并进行初始化操作
            Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
//            idField.set(entity, YitIdHelper.nextId());

            //3、获取当前时间戳
            Date date = new Date();
            long timestamp = date.getTime() / 1000;

            //4、获取属性名为createTime的属性并进行初始化操作
            Field createTimeField = clazz.getDeclaredField("gmtCreate");
            createTimeField.setAccessible(true);
            createTimeField.set(entity, new Timestamp(System.currentTimeMillis()));

            //5、获取属性名为updateTime的属性并进行初始化操作
            Field updateTimeField = clazz.getDeclaredField("gmtModified");
            updateTimeField.setAccessible(true);
            createTimeField.set(entity, new Timestamp(System.currentTimeMillis()));

            //6、返回数据
            return entity;
        } catch (NoSuchFieldException | IllegalAccessException e) {

            //7、抛出异常
            throw new IllegalArgumentException("PojoUtils--初始化对象失败", e);
        }
    }

    /**
     * 更新对象
     * <p>
     * 更新属性：updateTime
     *
     * @param entity
     * @return
     */
    public T setPojo(T entity) {
        try {
            //1、获取对象的类型
            Class<?> clazz = entity.getClass();

            //2、获取属性名为updateTime的属性并进行初始化操作
            Field updateTimeField = clazz.getDeclaredField("updateTime");
            updateTimeField.setAccessible(true);
            updateTimeField.set(entity, System.currentTimeMillis() / 1000);

            //3、返回数据
            return entity;
        } catch (NoSuchFieldException | IllegalAccessException e) {

            //4、抛出异常
            throw new IllegalArgumentException("PojoUtils--更新对象失败", e);
        }
    }

    /**
     * @author lzm
     * @date 2023/11/9 21:46
     * description: 将实体类转化成hashmap的形式,
     * 默认不转换驼峰
     * 默认不忽略null值
     * 默认没有黑名单
     */
    public static Map<String, Object> toMap(Object obj) {
        return toMap(obj, false, false, new ArrayList<>());
    }

    /**
     * @author lzm
     * @date 2023/11/9 21:46
     * description: 将实体类转化成hashmap的形式,
     * 默认没有黑名单
     */
    public static Map<String, Object> toMap(Object obj, Boolean camel, Boolean ignoreNull) {
        return toMap(obj, camel, ignoreNull, new ArrayList<>());
    }

    /**
     * @author lzm
     * @date 2023/11/9 21:46
     * description: 将实体类转化成hashmap的形式, camel参数：是否转换驼峰, ignoreNull参数：是否忽略null值，ignoredFields：黑名单
     */
    public static Map<String, Object> toMap(Object obj, Boolean camel, Boolean ignoreNull, List<String> ignoredFields) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();

            if (ignoredFields.contains(fieldName)) {
                continue; // 忽略指定的字段
            }

            if (camel) {
                fieldName = fieldName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
            }

            try {
                Object fieldValue = field.get(obj);

                if (!ignoreNull || fieldValue != null) {
                    map.put(fieldName, fieldValue);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }


}