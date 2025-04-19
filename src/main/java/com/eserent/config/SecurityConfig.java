package com.eserent.config;

import com.eserent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration class.
 * Handles authentication and authorization for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/home", "/register", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()  // Public access to common resources
                .antMatchers("/rooms/**").permitAll()  // Allow access to /rooms/** (so roomDetails is accessible)
                .antMatchers("/admin/**").hasRole("ADMIN")  // Restrict /admin/** to ADMIN role
                .antMatchers("/landlord/**").hasRole("LANDLORD")  // Restrict /landlord/** to LANDLORD role
                .antMatchers("/customer/**").hasRole("CUSTOMER")  // Restrict /customer/** to CUSTOMER role
                .anyRequest().authenticated()  // Any other request should be authenticated
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/dashboard")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/403");  // Custom 403 error page

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userService)  // Custom UserService for loading user details
                .passwordEncoder(passwordEncoder());  // Password encoding with BCrypt
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Define BCrypt password encoder
    }
}
