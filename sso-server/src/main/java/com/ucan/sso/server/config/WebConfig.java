package com.ucan.sso.server.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: Spring Mvc配置类
 * @author liming.cen
 * @date 2024-08-02 08:41:55
 * 
 */
public class WebConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://www.ucan.com", "http://ucan.com", "http://www.umall.com", "http://umall.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowCredentials(true);
    }

}
