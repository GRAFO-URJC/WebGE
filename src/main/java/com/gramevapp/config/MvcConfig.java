package com.gramevapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// https://dzone.com/articles/spring-security-4-authenticate-and-authorize-users
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/403").setViewName("403");
    }

    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        //https://stackoverflow.com/questions/32843115/how-to-configure-spring-data-to-use-postgres-with-hibernate-without-xml
        driverManagerDataSource.setDriverClassName("org.postgresql.Driver");
        //MASTER
        driverManagerDataSource.setUrl("jdbc:postgresql://db:5432/webge");
        //DEVELOP
        //driverManagerDataSource.setUrl("jdbc:postgresql://localhost:5432/webge");
        driverManagerDataSource.setUsername("usuario");
        driverManagerDataSource.setPassword("01234");
        return driverManagerDataSource;
    }
}