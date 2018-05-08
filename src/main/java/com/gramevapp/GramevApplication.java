package com.gramevapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com"})
public class GramevApplication {

	public static void main(String[] args)
	{
		SpringApplication.run(GramevApplication.class, args);
	}
}
