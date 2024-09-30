package com.ucan.app1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.ucan.app1.dao")
public class UcanApplication_1 {

    public static void main(String[] args) {
        SpringApplication.run(UcanApplication_1.class, args);
    }
}
