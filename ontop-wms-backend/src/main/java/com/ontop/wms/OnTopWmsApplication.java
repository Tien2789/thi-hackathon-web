package com.ontop.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnTopWmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnTopWmsApplication.class, args);
    }
}
