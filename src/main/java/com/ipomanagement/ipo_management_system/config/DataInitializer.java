package com.ipomanagement.ipo_management_system.config;

import com.ipomanagement.ipo_management_system.domain.enums.Role;
import com.ipomanagement.ipo_management_system.repository.UserRepository;
import com.ipomanagement.ipo_management_system.service.auth.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, UserService userService) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userService.createUserByAdmin("admin", "admin@example.com", "admin123", Role.ADMIN);
            }
            if (!userRepository.existsByUsername("issuer1")) {
                userService.createUserByAdmin("issuer1", "issuer1@example.com", "issuer123", Role.ISSUER);
            }
        };
    }
}