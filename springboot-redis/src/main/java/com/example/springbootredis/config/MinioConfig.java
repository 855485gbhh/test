package com.example.springbootredis.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "minio")
@Configuration
@Component
public class MinioConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endpoint;
    private String readPath;

    @Bean
    public MinioClient minioClient() {
        return MinioClient
                .builder()
                .credentials(accessKey, secretKey)
                .endpoint(endpoint)
                .build();

    }
}
