package com.moviebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.moviebackend.mapper")
public class MovieBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieBackendApplication.class, args);
    }
}
