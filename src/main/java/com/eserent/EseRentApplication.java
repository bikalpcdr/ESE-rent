package com.eserent;

import com.eserent.entity.User;
import com.eserent.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EseRentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EseRentApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSuperAdmin(UserService userService) {
        return args -> {
            // Check if super admin already exists
            if (userService.getUsersByRole(User.Role.ROLE_SUPER_ADMIN).isEmpty()) {
                User superAdmin = new User();
                superAdmin.setUsername("superadmin");
                superAdmin.setPassword("admin123");
                superAdmin.setEmail("superadmin@eserent.com");
                superAdmin.setFullName("Super Administrator");
                superAdmin.setRole(User.Role.ROLE_SUPER_ADMIN);
                superAdmin.setEnabled(true);

                try {
                    userService.createUser(superAdmin);
                    System.out.println("Super Admin user created successfully!");
                } catch (Exception e) {
                    System.err.println("Failed to create Super Admin user: " + e.getMessage());
                }
            }
        };
    }
} 