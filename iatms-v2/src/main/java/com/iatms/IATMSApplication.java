package com.iatms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.iatms.infrastructure.persistence.mapper")
public class IATMSApplication {

    public static void main(String[] args) {
        SpringApplication.run(IATMSApplication.class, args);
    }
}
