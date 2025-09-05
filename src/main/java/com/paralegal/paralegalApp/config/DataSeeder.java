package com.paralegal.paralegalApp.config;

import com.paralegal.paralegalApp.Enum.Role;
import com.paralegal.paralegalApp.Model.User;
import com.paralegal.paralegalApp.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository repo) {
        var encoder = new BCryptPasswordEncoder();
        return args -> {
            if (repo.findByEmail("admin@example.com").isEmpty()) {
                repo.save(User.builder()
                        .userName("Admin")
                        .email("admin@example.com")
                        .password(encoder.encode("Admin@123"))
                        .roles(Set.of(Role.ROLE_ADMIN))
                        .build());
            }
            if (repo.findByEmail("user@example.com").isEmpty()) {
                repo.save(User.builder()
                        .userName("Regular")
                        .email("user@example.com")
                        .password(encoder.encode("User@123"))
                        .roles(Set.of(Role.ROLE_USER))
                        .build());
            }
        };
    }
}
