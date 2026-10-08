package com.occupify.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OccupifyNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OccupifyNotificationApplication.class, args);
    }
}

