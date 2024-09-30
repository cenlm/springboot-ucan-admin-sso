package com.ucan.app2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.ucan.app2.dao")
public class UcanApplication_2 {

    public static void main(String[] args) {
        SpringApplication.run(UcanApplication_2.class, args);
    }
}
