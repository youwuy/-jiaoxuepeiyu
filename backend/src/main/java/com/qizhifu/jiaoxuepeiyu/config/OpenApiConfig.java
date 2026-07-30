package com.qizhifu.jiaoxuepeiyu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jiaoxuePeiyuOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Web教辅系统 API")
                        .version("0.1.0")
                        .description("Web教辅系统后台接口文档"));
    }
}
