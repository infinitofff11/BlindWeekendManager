package com.blindweekend.manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置 - 跨域支持
 *
 * 注: UTF-8 编码由 application.yml 中的 server.servlet.encoding 配置自动生效，
 *     Spring Boot HttpEncodingAutoConfiguration 会自动注册 CharacterEncodingFilter，
 *     无需在此手动定义以避免 Bean 名称冲突。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
