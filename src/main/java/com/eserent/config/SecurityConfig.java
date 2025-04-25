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
                .antMatchers("/", "/home", "/register", "/css/**", "/js/**", "/images/**", "/h2-console/**", "/uploads/**").permitAll()  // Public access to common resources
                .antMatchers("/rooms").permitAll()  // Allow public access to rooms listing
                .antMatchers("/rooms/*").authenticated()  // Restrict room details to authenticated users
                .antMatchers("/super-admin/**", "/admin/**").hasRole("SUPER_ADMIN")  // Restrict both /super-admin/** and /admin/** to SUPER_ADMIN role
                .antMatchers("/landlord/**").hasRole("LANDLORD")  // Restrict /landlord/** to LANDLORD role
                .antMatchers("/customer/**").hasRole("CUSTOMER")  // Restrict /customer/** to CUSTOMER role
                .antMatchers("/access-denied", "/403").permitAll()  // Make error pages publicly accessible
                .anyRequest().authenticated()  // Any other request should be authenticated
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/dashboard")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/access-denied");  // Use /access-denied for consistency
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
