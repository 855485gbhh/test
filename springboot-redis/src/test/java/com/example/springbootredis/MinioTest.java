package com.example.springbootredis;

import com.example.springbootredis.config.MinioConfig;
import com.example.springbootredis.utils.MinioUtils;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
public class MinioTest implements BeanPostProcessor {
    @Autowired
    private MinioClient minioClient;

    @Test
    void doing() throws Exception {
        File file = new File("C:\\Users\\asus\\Desktop\\镜像.png");
        FileInputStream fileInputStream = new FileInputStream(file);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket("redis")
                .object("1.png")
                .stream(fileInputStream, fileInputStream.available(), -1)
                .contentType("png/img").build());
    }

    @Test
    void bulk() throws Exception {
        String name = "1.png";
        String path = "C:\\Users\\asus\\Desktop\\" + name;
        minioClient.downloadObject(DownloadObjectArgs.builder()
                .bucket("redis")
                .object(name)
                .filename(path)
                .build());
    }

    @Test
    void stream() {
        List<Integer> collect = Stream.iterate(0, i -> i++).limit(100).collect(Collectors.toList());
        collect.forEach(System.out::println);
    }

    @Test
    void upload() {
        MinioUtils.upload("redis", "", "1.txt", "C:\\Users\\asus\\Desktop\\1.txt");
    }

    @Test
    void test() throws Exception {
        //  MinioUtils.uploadByDate("redis",null,"C:\\Users\\asus\\Desktop\\镜像.png");
        // MinioUtils.removeObjects("redis", null, "1.png", "2.png");
        MinioUtils.removeObjects("redis", Collections.singletonList("1.png"));
    }

    @Test
    void url(){
        String url = MinioUtils.downloadUrl("redis", "1.png", 1, TimeUnit.HOURS);
        System.out.println("url = " + url);
    }

    @Test
    void unit(){
        long seconds = TimeUnit.MILLISECONDS.toSeconds(900);
        System.out.println("seconds = " + seconds);
    }

    @Test
    void tag(){
        Map<String,String> map=new HashMap<>();
        map.put("txt","文件");
        MinioUtils.setObjectTags("redis","1.txt",map);
    }
}
