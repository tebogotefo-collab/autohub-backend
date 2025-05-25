package com.mathotech.autopartshub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class AutoPartsHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoPartsHubApplication.class, args);
    }
}
