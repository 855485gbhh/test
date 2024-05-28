package com.example.springbootredis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createInfo("springboot-reddis", "1.0"));
    }

    private Info createInfo(String title, String version) {
        return new Info()
                .title(title)
                .version(version)
                .description("");
//                .termsOfService("http://条款网址")
//                .license(
//                        new License().name("Apache 2.0")
//                                .url("http://doc.xiaominfo.com"));
    }


}
