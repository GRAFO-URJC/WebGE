package com.gramevapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// https://dzone.com/articles/spring-security-4-authenticate-and-authorize-users
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/403").setViewName("access-denied");
    }

    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource(@Value("${spring.datasource.url}") String url,
                                              @Value("${spring.datasource.username}") String username,
                                              @Value("${spring.datasource.password}") String password) {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        //https://stackoverflow.com/questions/32843115/how-to-configure-spring-data-to-use-postgres-with-hibernate-without-xml
        driverManagerDataSource.setDriverClassName("org.postgresql.Driver");
        //MASTER
        driverManagerDataSource.setUrl(url);
        //DEVELOP
        //driverManagerDataSource.setUrl("jdbc:postgresql://localhost:5432/webge");
        driverManagerDataSource.setUsername(username);
        driverManagerDataSource.setPassword(password);
        return driverManagerDataSource;
    }
}