package com.yuelutraffic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class YueluTrafficBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YueluTrafficBackendApplication.class, args);
    }
}
