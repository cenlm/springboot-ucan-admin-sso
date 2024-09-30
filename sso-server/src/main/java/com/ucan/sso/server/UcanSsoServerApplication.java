package com.ucan.sso.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.ucan.sso.server.dao")
public class UcanSsoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UcanSsoServerApplication.class, args);
    }
}
