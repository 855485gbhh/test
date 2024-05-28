package com.example.springbootredis.controller;

import com.example.springbootredis.pojo.response.JsonResponse;
import com.example.springbootredis.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/info")
    public JsonResponse<Object> info(@RequestBody MultipartFile file) {
    return fileService.info(file);
    }

    @PostMapping("/up")
    public JsonResponse<Object> up(String name, String filePath) throws Exception {
        return fileService.upload(name, filePath);
    }

    @PostMapping("/upload")
    public JsonResponse<Object> up(String name, MultipartFile file) throws Exception {
        return fileService.upload(name, file);
    }


    @PostMapping("/put")
    public JsonResponse<Object> put(String name, String filePath) throws Exception {
        return fileService.put(name, filePath);
    }

    @GetMapping("/get")
    public JsonResponse<Object> get(@RequestParam("name") String name, @RequestParam(required = false) String filePath) {
        return fileService.getObject(name, filePath);
    }

    @DeleteMapping("/delete")
    public JsonResponse<Object> delete(@RequestParam("name") String name) {
        return fileService.delete(name);
    }

    @GetMapping("/download")
    public JsonResponse<Object> download(String name, String filePath) {
        return fileService.download(name, filePath);
    }

    @GetMapping("/objectinfo")
    public JsonResponse<Object> info(String name) {
        return fileService.getObjectInfo(name);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("/upl")
    public JsonResponse<Object> up(Map<String, String> map) throws Exception {
        return fileService.upload(map);
    }

    @PostMapping("/putl")
    public JsonResponse<Object> put(Map<String, String> map) throws Exception {
        return fileService.put(map);
    }

//    @GetMapping("/get")
//    public JsonResponse<Object> get(@RequestParam("name") String name,@RequestParam(required = false) String filePath){
//        return fileService.getObject(name,filePath);
//    }

    @DeleteMapping("/deletel")
    public JsonResponse<Object> delete(List<String> list) {
        return fileService.delete(list);
    }

    @GetMapping("/downloadl")
    public JsonResponse<Object> download(Map<String, String> map) {
        return fileService.download(map);
    }

    @PutMapping("/infol")
    public JsonResponse<Object> info(@RequestBody List<String> list) {
        return fileService.getObjectInfo(list);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @PostMapping("/folder")
    public JsonResponse<Object> uploadFolder(@RequestBody MultipartFile file) {
        return fileService.uploadFolder(file);
    }

    @PostMapping("/folder/create")
    public JsonResponse<Object> folderCreate(@RequestParam String bucket,
                                             @RequestParam String folderPath) {
        return fileService.createFolder(bucket,folderPath);
    }
    @PostMapping("/files")
    public JsonResponse<Object> uploadFiles(@RequestBody MultipartFile[] files) {
        return fileService.uploadFiles(files);
    }

}
