package com.example.springbootredis.utils;

import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.ranges.RangeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.minio.GetPresignedObjectUrlArgs.DEFAULT_EXPIRY_TIME;

@Component
@Slf4j
public class MinioUtils {

    private static MinioConfig minioConfig;

    private static MinioClient minioClient;

    private final static String separator = "/";

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Autowired
    public void setMinioConfig(MinioConfig minioConfig) {
        this.minioConfig = minioConfig;
    }


    /**
     * 判断文件是否存在
     *
     * @param bucketName
     * @return
     */
    public static boolean existObject(String bucketName, String objectName) {
        // 若数据库中存在，根据数据库中的文件信息，则继续判断bucket中是否存在
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            if (inputStream == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 判断bucket是否存在
     *
     * @param bucketName
     * @return
     */
    public static boolean existBucket(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            bucketName = minioConfig.getBucket();
        }
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 判断bucket是否存在，不存在则创建, 默认为yml配置的 bucket
     */
    public static String existBucketAndCreate(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            bucketName = minioConfig.getBucket();
        }
        try {
            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("minio exist Bucket error.", e);
            throw new RuntimeException("判断bucket是否存在，不存在则创建,失败");
        }
        return bucketName;
    }


    /**
     * 删除存储bucket
     *
     * @param bucketName 存储bucket名称，必填
     * @return Boolean
     */
    public static boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("minio remove Bucket error.", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 上传文件
     */

    /**
     * @param bucketName 存储bucket名称
     * @param folderName 文件所在的文件夹，以 / 为分隔符
     * @param objectName 文件名称
     * @param filePath   准备上传的文件绝对路径
     * @return
     */
    public static boolean upload(String bucketName, String folderName, String objectName, String filePath) {
//        String objectSuffix = objectName.split("\\.")[1];
//        System.out.println("objectSuffix = " + objectSuffix);
        String objectSuffix = MediaTypeFactory.getMediaType(objectName).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        System.out.println("objectSuffix = " + objectSuffix);
        String fileSuffix = filePath.split("\\.")[1];
        checkType(objectSuffix, fileSuffix);
//        if (!objectSuffix.contains(fileSuffix)) {
//            throw new RuntimeException("文件后缀名不匹配");
//        }
        if (folderName == null || "".equals(folderName)) {
            folderName = "";
        } else {
            folderName = folderName + "/";
        }
        try {
            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(folderName + objectName)
                    .filename(filePath)
                    .contentType(objectSuffix)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("minio put file error.", e);
            throw new RuntimeException("上传到minio出错");
        }
    }

    public static boolean upload(String bucketName, List<String> filePaths) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static boolean checkType(String objectSuffix, String fileSuffix) {
        return false;
    }

    /**
     * 文件上传(根据本地文件)，以日期为格式
     *
     * @param bucketName
     * @param folderName
     * @param filePath   本地文件全路径 D:/xx/xx.text
     * @return
     */
    public static boolean uploadByDateUUID(String bucketName, String folderName, String filePath) {
        //判断并创建Bucket
        bucketName = existBucketAndCreate(bucketName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String format = dateFormat.format(new Date());
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(folderName)) {
            stringBuilder.append(folderName).append(separator);
        }
        String suffix = MediaTypeFactory.getMediaType(filePath).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        stringBuilder.append(format).append(separator).append(uuid);
        try {
            // 上传到minio服务器
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .filename(filePath)
                    .object(stringBuilder.toString())
                    .contentType(suffix)
                    .build());
//            return minioConfig.getReadPath() + separator + minioConfig.getBucket() +
//                    separator +
//                    stringBuilder.toString();
            return true;
        } catch (Exception e) {
            log.error("minio put file error.", e);
            throw new RuntimeException("上传文件失败");
        }
    }

    public static boolean uploadByDate(String bucketName, String folderName, String objectName, String filePath) {
        //判断并创建Bucket
        bucketName = existBucketAndCreate(bucketName);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String format = dateFormat.format(new Date());
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(folderName)) {
            stringBuilder.append(folderName).append(separator);
        }
        stringBuilder.append(format).append(separator);
        String[] split = filePath.split("\\\\");
        String file = split[split.length - 1];

        stringBuilder.append(file);
        if (objectName == null || "".equals(objectName)) {
            objectName = file;
        }
        String suffix = MediaTypeFactory.getMediaType(objectName).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        try {
            // 上传到minio服务器
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath)
                    .object(stringBuilder.toString())
                    .contentType(suffix)
                    .build());
//            return minioConfig.getReadPath() + separator + minioConfig.getBucket() +
//                    separator +
//                    stringBuilder.toString();
            return true;
        } catch (Exception e) {
            log.error("minio put file error.", e);
            throw new RuntimeException("上传文件失败");
        }
    }

    public static boolean uploadByUUID(String bucketName, String folderName, String filePath) {
        //判断并创建Bucket
        bucketName = existBucketAndCreate(bucketName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(folderName)) {
            stringBuilder.append(folderName).append(separator);
        }
        String suffix = MediaTypeFactory.getMediaType(filePath).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        stringBuilder.append(uuid).append(".").append(suffix);
        try {
            // 上传到minio服务器
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .filename(filePath)
                    .object(stringBuilder.toString())
                    .contentType(suffix)
                    .build());
//            return minioConfig.getReadPath() + separator + minioConfig.getBucket() +
//                    separator +
//                    stringBuilder.toString();
            return true;
        } catch (Exception e) {
            log.error("minio put file error.", e);
            throw new RuntimeException("上传文件失败");
        }
    }


    /**
     * 文件上传(根据流),文件路径以日期为格式
     *
     * @param bucketName
     * @param folderName
     * @param file
     * @return
     */
    public static boolean upload(String bucketName, String folderName, MultipartFile file) {
        //判断并创建Bucket
        bucketName = existBucketAndCreate(bucketName);
        //  String filePath = builderFilePath(folderName, file);
        try {
            InputStream inputStream = file.getInputStream();
            // 上传到minio服务器
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(file.getOriginalFilename())
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(getContentType(file))
                    .build());
//            return minioConfig.getReadPath() + separator + minioConfig.getBucket() +
//                    separator +
//                    filePath;
        } catch (Exception e) {
            log.error("minio put file error.", e);
            throw new RuntimeException("上传文件失败");
        }
        return true;
    }

    public static boolean upload(String bucketName, String folderName, MultipartFile[] files) {
        //判断并创建Bucket
        bucketName = existBucketAndCreate(bucketName);
        for (MultipartFile file : files) {
            String filePath = builderFilePath(folderName, file);
            try {
                InputStream inputStream = file.getInputStream();
                // 上传到minio服务器
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .stream(inputStream, inputStream.available(), -1)
                        .contentType(file.getContentType())
                        .build());
//                return minioConfig.getReadPath() + separator + minioConfig.getBucket() +
//                        separator +
//                        filePath;
            } catch (Exception e) {
                log.error("minio put file error.  object:{}", files, e);
                //throw new RuntimeException("上传文件失败");
            }
        }
        return true;
    }

//    /**
//     * 上传图片文件
//     *
//     * @param bucketName    bucket名称，为空时默认yml配置
//     * @param prefix        文件前缀
//     * @param multipartFile 文件
//     * @return 文件全路径
//     */
//    public static String uploadImgFile(String bucketName, String prefix, MultipartFile multipartFile) {
//        //判断并创建Bucket
//        bucketName = existBucketAndCreate(bucketName);
//        //构建文件存储路径
//        String filePath = builderFilePath(prefix, multipartFile);
//
//        try {
//            InputStream inputStream = multipartFile.getInputStream();
//            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
//                    .object(filePath)
//                    .contentType("image/jpg/png")
//                    .bucket(bucketName)
//                    .stream(inputStream, inputStream.available(), -1)
//                    .build();
//            minioClient.putObject(putObjectArgs);
//            return minioConfig.getReadPath() + separator + minioConfig.getBucket() +
//                    separator +
//                    filePath;
//        } catch (Exception ex) {
//            log.error("minio put image file error.", ex);
//            throw new RuntimeException("上传文件失败");
//        }
//    }

    /**
     * 下载文件
     *
     * @param bucketName bucket存储桶
     * @param folderName
     * @param objectName
     * @param path
     */
    public static void download(String bucketName, String folderName, String objectName, String path) {
        if (folderName == null) {
            folderName = "";
        }
        try {
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(folderName + "/" + objectName)
                    .filename(path)
                    .build());
        } catch (Exception e) {
            log.error("minio down file error.  object:{}", folderName + "/" + objectName);
            throw new RuntimeException("下载文件失败");
        }
    }

    /**
     * 下载文件
     *
     * @param bucketName 必填
     * @param path       文件全路径
     * @return 文件流
     */
    public static byte[] downLoadFile(String bucketName, String path) {
        String key = path.replace(minioConfig.getEndpoint() + "/", "");
        int index = key.indexOf(separator);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
        } catch (Exception e) {
            log.error("minio down file error.  objectPath:{}", path);
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (!((rc = inputStream.read(buff, 0, 100)) > 0)) {
                    break;
                }
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * @param bucketName
     * @param folderPath 分片文件路径
     * @param chunkTotal
     * @param fileName   合并后的文件名
     * @return
     */
    public static boolean mergeChunkFiles(String fileMd5, String bucketName, String folderPath, int chunkTotal, String fileName) {
        List<ComposeSource> sources = Stream.iterate(0, i -> i++).limit(chunkTotal).map(i -> ComposeSource
                .builder()
                .bucket(minioConfig.getBucket())
                .object(folderPath + i).build()).collect(Collectors.toList());

        // 合并后文件的名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String objectName = getFilePathByMd5(fileMd5, extension);
        //指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .sources(sources)
                .build();
        //合并文件,单个分片文件大小必须为5m
        try {
            minioClient.composeObject(composeObjectArgs);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错了，bucket:{},objectName:{},错误信息:{}", minioConfig.getBucket(), objectName, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储bucket名称，必填
     * @param objectPath 文件全路径
     */
    public static boolean deleteFile(String bucketName, String objectPath) {
        String key = objectPath.replace(minioConfig.getEndpoint() + "/", "");
        int index = key.indexOf(separator);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  objectPath:{}", objectPath);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean removeObject(String bucketName, String folderName, String objectName) {
        if (folderName == null) {
            folderName = "";
        }

        // 删除Object
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().
                    bucket(bucketName)
                    .object(folderName + "/" + objectName)
                    .build());
        } catch (Exception e) {
            log.error("minio remove file error.  objectPath:{}", folderName + "/" + objectName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 批量删除文件对象
     *
     * @param bucketName  存储bucket名称，必填
     * @param objectPaths 对象全路径集合
     */
    public static void removeObjects(String bucketName, List<String> objectPaths) {
        Map<String, String> resultMap = new HashMap<>();
        List<DeleteObject> dos = objectPaths.stream().map(e -> new DeleteObject(e)).collect(Collectors.toList());
        Iterable<Result<DeleteError>> iterable = null;
        try {
            iterable = minioClient.removeObjects(RemoveObjectsArgs
                    .builder()
                    .bucket(bucketName)
                    .objects(dos)
                    .build());

        } catch (Exception e) {
            if (iterable != null) {
                for (Result<DeleteError> errorResult : iterable) {
                    log.error("minio remove file error.  objectPath:{}", objectPaths);
                }
            }
        }
    }

    public static void removeObjects(String bucketName, String folderName, List<String> objects) {
        String path;
        if (folderName == null || "".equals(folderName)) {
            path = bucketName;
        } else {
            path = bucketName + "/" + folderName;
        }
        final String prefix = path;
        List<DeleteObject> dos = objects.stream().map(object -> new DeleteObject(prefix + "/" + object)).collect(Collectors.toList());

        Iterable<Result<DeleteError>> iterable = null;
        try {
            iterable = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());

        } catch (Exception e) {
            try {
                if (iterable != null) {
                    for (Result<DeleteError> errorResult : iterable) {
                        DeleteError error = errorResult.get();
                        String resource = error.resource();
                        log.error("minio remove file error.  objectPath:{}", resource);
                    }
                }
            } catch (Exception ex) {
                log.info("处理删除文件错误失败", iterable);
                throw new RuntimeException(ex);
            }
        }
    }

    public static void removeObjects(String bucketName, String folderName, String... objects) {
        String path;
        if (folderName == null || "".equals(folderName)) {
            path = "";
        } else {
            path = folderName;
        }
        final String prefix = path;
        List<DeleteObject> dos = Arrays.stream(objects).map(object -> {
            String url = prefix + object;
            System.err.println(url);
            return new DeleteObject(url);
        }).collect(Collectors.toList());

        Iterable<Result<DeleteError>> iterable = null;
        try {
            iterable = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
        } catch (Exception e) {
            try {
                if (iterable != null) {
                    for (Result<DeleteError> errorResult : iterable) {
                        DeleteError error = errorResult.get();
                        String resource = error.resource();
                        log.error("minio remove file error.  objectPath:{},message:{}", resource, error.toString());
                    }
                }
            } catch (Exception ex) {
                log.info("处理删除文件错误失败");
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 文件存储路径构建 yyyy/mm/dd/uuid.jpg
     *
     * @param prefix
     * @param multipartFile
     * @return
     */
    public static String builderFilePath(String prefix, MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        assert filename != null;
        String suffix = filename.split("\\.")[1];
        String uuid = UUID.randomUUID().toString().replace("-", "");
        StringBuilder stringBuilder = new StringBuilder(50);
        if (StringUtils.isNotBlank(prefix)) {
            stringBuilder.append(prefix).append(separator);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(separator).append(uuid);
        stringBuilder.append(".").append(suffix);
        return stringBuilder.toString();
    }

    public static List<String> getAll(String bucketName) {
        boolean isExist = existBucket(bucketName);
        if (isExist == false) {
            throw new RuntimeException("该存储桶不存在");
        }

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .build());
            List<String> list = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                String str = JSONObject.toJSONString(item);
                list.add(str);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 合并后完整视频路径
     *
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private static String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    public static String getContentType(String filePath) {
        return MediaTypeFactory.getMediaType(filePath).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
    }

    public static String getContentType(MultipartFile file) {
        return MediaTypeFactory.getMediaType(file.getOriginalFilename()).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
    }

    public static boolean makeDir(String bucket, String folderPath) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(folderPath + "/")
                    .filename("")
                    .build());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * 生成一个给HTTP GET请求用的presigned URL。
     * 浏览器/移动端的客户端可以用这个URL进行下载，即使其所在的存储桶是私有的。这个presigned URL可以设置一个失效时间，默认值是7天。
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     *                   //    * @param expires    失效时间（以秒为单位），默认是7天，不得大于七天
     * @return
     */
    public static String downloadUrl(String bucketName, String objectName, Integer expires, TimeUnit timeUnit) {
        boolean flag = existBucket(bucketName);
        String url = "";
        if (flag) {
            if (timeUnit.toSeconds(expires) < 1 || timeUnit.toDays(expires) > 7) {
                throw new RuntimeException(
                        "expiry must be minimum 1 second to maximum 7 days");
            }
            try {
                url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expires, timeUnit)
                        //动态参数
//                       .expiry(24 * 60 * 60)//用秒来计算一天时间有效期
//                        .expiry(1, TimeUnit.DAYS)//按天传参
//                        .expiry(1, TimeUnit.HOURS)//按小时传参数
                        .build());
            } catch (Exception e) {
                log.error("Exception", e);
                throw new RuntimeException("生成文件下载url失败");
            }
        }
        return url;
    }

    /**
     * 生成一个给HTTP PUT请求用的presigned URL。
     * 浏览器/移动端的客户端可以用这个URL进行上传，即使其所在的存储桶是私有的。这个presigned URL可以设置一个失效时间，默认值是7天。
     *
     * @param bucketName 存储桶名称
     * @param fileName   存储桶里的对象名称
     * @param expires    失效时间（以秒为单位），默认是7天，不得大于七天
     * @return String
     */
    public static String uploadUrl(String bucketName, String fileName, Integer expires, TimeUnit timeUnit) {
        boolean flag = existBucket(bucketName);
        String url = "";
        if (flag) {
            if (timeUnit.toSeconds(expires) < 1 || timeUnit.toDays(expires) > 7) {
                try {
                    throw new RuntimeException(
                            "expiry must be minimum 1 second to maximum 7 days");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucketName)
                        .object(fileName)
                        .expiry(expires, timeUnit)//动态参数
//                        .expiry(24 * 60 * 60)//用秒来计算一天时间有效期
//                        .expiry(1, TimeUnit.DAYS)//按天传参
//                        .expiry(1, TimeUnit.HOURS)//按小时传参数
                        .build());
            } catch (Exception e) {
                log.error("Exception:{}", e);
                throw new RuntimeException("生成文件上传url失败");
            }
        }
        return url;
    }

    public static boolean setBucketTags(String bucketName, Map<String, String> tagMap) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {

            //判断bucket是否存在
            boolean res = existBucket(bucketName);
            if (res == false) {
                throw new RuntimeException("该存储桶不存在");
            }
            //有即为bucket设置Tags标签
            //定义多标签
//            Map<String, String> tagMap = new HashMap<>();
//            tagMap.put("Project", " Project One");
//            tagMap.put("Belong to", "Lu Lu");
            //设置多标签
            minioClient.setBucketTags(
                    SetBucketTagsArgs.builder()
                            .bucket(bucketName)
                            .tags(tagMap)
                            .build());
        } catch (Exception e) {
            log.error("Exception:{}", e);
            throw new RuntimeException("存储桶添加标签失败");
        }
        return true;
    }

    public static boolean setObjectTags(String bucketName, String objectName, Map<String, String> tagMap) {
        //判断bucket与object是否存在
         checkObject(bucketName,objectName);
        try {
            minioClient.setObjectTags(SetObjectTagsArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .tags(tagMap).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    public static boolean checkBucket(String bucketName){
        boolean existedBucket = existBucket(bucketName);
        if(existedBucket==false){
            throw new RuntimeException("该存储桶不存在");
        }
        return true;
    }
    public static boolean checkObject(String bucketName,String objectName){
        boolean existedBucket = existBucket(bucketName);
        if(existedBucket==false){
            throw new RuntimeException("该存储桶不存在");
        }
        boolean existedObject = existObject(bucketName, objectName);
        if(existedObject==false){
            throw new RuntimeException("该文件不存在");
        }
        return true;
    }

    public static void test(){}
}