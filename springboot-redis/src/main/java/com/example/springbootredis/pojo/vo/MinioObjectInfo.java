package com.example.springbootredis.pojo.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.minio.StatObjectResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
public class MinioObjectInfo {
    private String bucket;
    private String name;
    private ZonedDateTime lastModified;
    private Long size;

    public MinioObjectInfo(StatObjectResponse response) {
        this.bucket = response.bucket();
        this.name = response.object();
        this.lastModified = response.lastModified();
        this.size = response.size();
    }
}
