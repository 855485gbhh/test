package com.example.springbootredis.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.pojo.po.FileInfo;
import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.pojo.vo.MinioObjectInfo;
import com.example.springbootredis.service.FileService;
import com.example.springbootredis.utils.MinioUtils;
import com.example.springbootredis.utils.ResponseUtils;
import io.minio.*;

import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private MinioClient minioClient;

    @Override
    public JsonResponse<Object> upload(String name, String filePath) {


        try {
            ObjectWriteResponse response = minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket("redis")
                    .object(name)
                    .filename(filePath)
                    .build());
            return ResponseUtils.success("文件上传成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonResponse<Object> upload(String name, MultipartFile file) {
        boolean uploaded = MinioUtils.upload("redis", null, file);
        return ResponseUtils.success(uploaded);
    }

    @Override
    public JsonResponse<Object> put(String name, String filePath) {


        try {
            InputStream stream = new FileInputStream(new File(filePath));
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("redis")
                    .object(name)
                    .stream(stream, stream.available(), -1)
                    .build());
            return ResponseUtils.success("文件上传成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonResponse<Object> getObjectInfo(String name) {
        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket("redis")
                    .object(name).build());

            MinioObjectInfo info = new MinioObjectInfo(response);
            return ResponseUtils.success(info);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public JsonResponse<Object> upload(Map<String, String> map) {
        return null;
    }

    @Override
    public JsonResponse<Object> put(Map<String, String> map) {
        return null;
    }

    @Override
    public JsonResponse<Object> delete(List<String> list) {
        List<DeleteObject> collect = list.stream().map(fileName -> new DeleteObject(fileName)).collect(Collectors.toList());
        Iterable<Result<DeleteError>> iterable = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket("redis")
                .objects(collect).build());

        return ResponseUtils.success(iterable);
    }

    @Override
    public JsonResponse<Object> download(Map<String, String> map) {
        return null;
    }

    @Override
    public JsonResponse<Object> getObjectInfo(List<String> list) {
        return null;
    }

    @Override
    public JsonResponse<Object> uploadFolder(MultipartFile file) {
        boolean res = MinioUtils.upload("redis", null, file);
        log.info("上传文件成功");
        return ResponseUtils.success(res);
    }

    @Override
    public JsonResponse<Object> uploadFiles(MultipartFile[] files) {
        Arrays.stream(files).forEach(file -> {
            try {
                String contentType = MinioUtils.getContentType(file);
                InputStream stream = file.getInputStream();

                minioClient.putObject(PutObjectArgs.builder()
                        .stream(stream, stream.available(), -1)
                        .bucket("redis")
                        .object(file.getOriginalFilename())
                        .contentType(contentType)
                        .build());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
        return ResponseUtils.success("上传成功");
    }

    @Override
    public JsonResponse<Object> info(MultipartFile file) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(file.getName());
        fileInfo.setOriginalFilename(file.getOriginalFilename());
        fileInfo.setContentType(file.getContentType());
        return ResponseUtils.success(fileInfo);
    }

    @Override
    public JsonResponse<Object> createFolder(String bucket, String folderPath) {
        boolean res = MinioUtils.makeDir(bucket, folderPath);
        return ResponseUtils.success(res);

    }

    @Override
    public JsonResponse<Object> getObject(String filename, String filePath) {
        GetObjectResponse response = null;
        try {
            response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket("redis")
                    .object(filename)
                    .build());
            return ResponseUtils.success(JSONObject.toJSONString(response));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public JsonResponse<Object> delete(String name) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket("redis")
                    .object(name)
                    .build());
            return ResponseUtils.success("删除文件成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonResponse<Object> download(String name, String filePath) {
        try {
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket("redis")
                    .object(name)
                    .filename(filePath)
                    .build());
            return ResponseUtils.success("下载成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
