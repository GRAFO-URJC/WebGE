package com.gramevapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com"})
public class GramevApplication {
    private static ConfigurableApplicationContext app;

    public static void main(String[] args) {
        SpringApplication.run(GramevApplication.class, args);
    }

    public static void start() {
        start(new String[] {});
    }

    private static void start(String[] args) {
        if(app == null) {
            app = SpringApplication.run(GramevApplication.class, args);
        }
    }

    public static void stop() {
        if(app != null) {
            app.close();
        }
    }
}
