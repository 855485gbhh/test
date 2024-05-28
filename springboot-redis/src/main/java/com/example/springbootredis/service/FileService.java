package com.example.springbootredis.service;

import com.example.springbootredis.pojo.response.JsonResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileService {

    JsonResponse<Object> getObject(String filename,String filePath);

    JsonResponse<Object> delete(String name);

    JsonResponse<Object> download(String name, String filePath);

    JsonResponse<Object> upload(String name, String filePath);
    JsonResponse<Object> upload(String name, MultipartFile file);

    JsonResponse<Object> put(String name, String filePath);

    JsonResponse<Object> getObjectInfo(String name);

    JsonResponse<Object> upload(Map<String, String> map);

    JsonResponse<Object> put(Map<String, String> map);

    JsonResponse<Object> delete(List<String> list);

    JsonResponse<Object> download(Map<String, String> map);

    JsonResponse<Object> getObjectInfo(List<String> list);

    JsonResponse<Object> uploadFolder(MultipartFile file);

    JsonResponse<Object> uploadFiles(MultipartFile[] files);

    JsonResponse<Object> info(MultipartFile file);

    JsonResponse<Object> createFolder(String bucket, String folderPath);
}
