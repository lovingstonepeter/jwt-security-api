package com.example.jwtsecurity.config;

import com.example.jwtsecurity.entity.Role;
import com.example.jwtsecurity.entity.UserEntity;
import com.example.jwtsecurity.repository.RoleRepository;
import com.example.jwtsecurity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/** Seeds the two roles and one admin account on startup (idempotent). */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role userRole = findOrCreate(Role.USER);
        Role adminRole = findOrCreate(Role.ADMIN);

        if (!userRepository.existsByUsername(adminUsername)) {
            UserEntity admin = new UserEntity();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(new HashSet<>(Set.of(userRole, adminRole)));
            userRepository.save(admin);
            log.info("Seeded admin account '{}'", adminUsername);
        }
    }

    private Role findOrCreate(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(null, name)));
    }
}
