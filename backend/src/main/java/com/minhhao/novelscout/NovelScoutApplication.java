package com.minhhao.novelscout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NovelScoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelScoutApplication.class, args);
    }
}
