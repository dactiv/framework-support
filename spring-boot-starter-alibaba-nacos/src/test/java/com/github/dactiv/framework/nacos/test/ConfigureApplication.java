package com.github.dactiv.framework.nacos.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ConfigureApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigureApplication.class, args);
    }

}
