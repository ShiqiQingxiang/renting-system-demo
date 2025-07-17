package com.rental.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.List;

/**
 * SpringDoc OpenAPI 3.0 配置 - 租赁系统API文档
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("租赁系统 API")
                        .description("租赁系统后端API文档 - 基于SpringDoc OpenAPI 3.0，包含认证、用户管理、物品管理、订单管理等模块")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("租赁系统开发团队")
                                .email("dev@rental.com")
                                .url("https://rental.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("开发环境"),
                        new Server()
                                .url("https://api.rental.com")
                                .description("生产环境")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token 认证，格式: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * 认证模块API分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-认证模块")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * 用户管理模块API分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("02-用户管理")
                .pathsToMatch("/api/users/**")
                .build();
    }

    /**
     * 权限管理模块API分组
     */
    @Bean
    public GroupedOpenApi permissionApi() {
        return GroupedOpenApi.builder()
                .group("03-权限管理")
                .pathsToMatch("/api/roles/**", "/api/permissions/**")
                .build();
    }

    /**
     * 物品管理模块API分组
     */
    @Bean
    public GroupedOpenApi itemApi() {
        return GroupedOpenApi.builder()
                .group("04-物品管理")
                .pathsToMatch("/api/items/**", "/api/categories/**", "/api/item-categories/**")
                .build();
    }

    /**
     * 订单管理模块API分组
     */
    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("05-订单管理")
                .pathsToMatch("/api/orders/**")
                .build();
    }

    /**
     * 支付管理模块API分组
     */
    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("06-支付管理")
                .pathsToMatch("/api/payments/**")
                .build();
    }

    /**
     * 合同管理模块API分组
     */
    @Bean
    public GroupedOpenApi contractApi() {
        return GroupedOpenApi.builder()
                .group("07-合同管理")
                .pathsToMatch("/api/contracts/**")
                .build();
    }

    /**
     * 财务管理模块API分组
     */
    @Bean
    public GroupedOpenApi financeApi() {
        return GroupedOpenApi.builder()
                .group("08-财务管理")
                .pathsToMatch("/api/finance/**")
                .build();
    }

    /**
     * 系统管理模块API分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("09-系统管理")
                .pathsToMatch("/api/system/**", "/actuator/**")
                .build();
    }
}
