package com.sangam.abcbank.userservice.config;

import com.sangam.abcbank.userservice.model.Role;
import com.sangam.abcbank.userservice.model.User;
import com.sangam.abcbank.userservice.repository.RoleRepository;
import com.sangam.abcbank.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds default roles (ROLE_ADMIN, ROLE_USER) and a default admin account
 * on first startup so the API is immediately usable.
 *
 * Default admin credentials: admin / Admin@123
 * Change or remove this seeder before deploying to production.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@abcbank.com")
                    .fullName("System Administrator")
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
        }
    }
}
