package com.unigear.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.unigear.tracker.features.auth.config.AuthEmailProperties;

@SpringBootApplication
@EnableConfigurationProperties(AuthEmailProperties.class)
@ComponentScan(basePackages = {"com.unigear.tracker.features", "com.unigear.tracker.pattern", "com.unigear.tracker.config"})
public class UniGearTrackerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UniGearTrackerApplication.class, args);
        System.out.println("UniGear Tracker Backend is running on port 8080");
    }
}
