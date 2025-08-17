package com.c203.autobiography;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AutobiographyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutobiographyApplication.class, args);
    }

}
