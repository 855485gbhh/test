package com.example.springbootredis.pojo.po;

import lombok.Data;

@Data
public class FileInfo {
    private String Name;
    private String OriginalFilename;
    private String contentType;
}
