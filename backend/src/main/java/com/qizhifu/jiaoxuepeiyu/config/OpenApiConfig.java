package com.qizhifu.jiaoxuepeiyu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
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
                        .description("Web教辅系统后台接口文档，覆盖管理端、学员端、UE三维程序和测试联调接口。"))
                .components(new Components()
                        .addSecuritySchemes("UserIdHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")));
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi studentApi() {
        return GroupedOpenApi.builder()
                .group("student")
                .pathsToMatch("/api/student/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**", "/api/health")
                .build();
    }
}
