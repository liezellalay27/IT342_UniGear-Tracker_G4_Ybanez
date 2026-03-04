package com.unigear.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UniGearTrackerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UniGearTrackerApplication.class, args);
        System.out.println("UniGear Tracker Backend is running on port 8080");
    }
}
