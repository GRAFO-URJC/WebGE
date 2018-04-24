package com.gramevapp.config;

import com.gramevapp.web.LoggingAccessDeniedHandler;
import javax.sql.DataSource;
import com.gramevapp.web.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig
    extends WebSecurityConfigurerAdapter {

    @Autowired
    private LoggingAccessDeniedHandler accessDeniedHandler;

    public static final String REALM_NAME = "webge.com";

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Qualifier("dataSource")
    @Autowired
    DataSource dataSource;

    @Override
    public void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select email, password, enabled from user where email=?")
                .authoritiesByUsernameQuery("select email, role_name from users_roles where email=?")
                .passwordEncoder(passwordEncoder());    // When user is doing the login -> We will encode the password with the Hash. thus we will compare the DB password with the user
        // auth.authenticationProvider(authenticationProvider());   // If we turn on this - Our login authenticator will not be the D.B.
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers(
                        "/registration",
                        "/",
                        "/js/**",
                        "/css/**",
                        "/css/main.css",
                        "/img/**",
                        "/assets/**",
                        "/webjars/**").permitAll()
                .antMatchers("/user/**").hasRole("USER")    // Only users can access to /user/whatever
                .antMatchers("/admin/**").access("hasAnyAuthority('ADMIN')")
                .anyRequest().authenticated()
            .and()
                .formLogin().loginPage("/login")
                .permitAll()
            .and()
            .logout()
                .invalidateHttpSession(true)
                //.clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            .and()
            .exceptionHandling()
             .accessDeniedHandler(accessDeniedHandler)
            .and()
                .httpBasic()
                    .realmName(REALM_NAME)
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint());    // CustomAuthenticationEntryPoint created

        http.csrf().disable();
        http.headers().frameOptions().disable();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(myUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}