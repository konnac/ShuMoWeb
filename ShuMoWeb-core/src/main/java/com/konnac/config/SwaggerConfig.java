package com.konnac.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShuMo Management API")
                        .version("1.0.0")
                        .description("舒莫项目管理平台后端API接口文档")
                        .contact(new Contact()
                                .name("Konnac")
                                .email("support@konnac.com")));
    }
}