package com.example.vacation_management.config;

import com.example.vacation_management.entity.Role;
import com.example.vacation_management.entity.User;
import com.example.vacation_management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
            logger.info("No admin users found. Creating default admin user...");

            User admin = new User();
            admin.setEmail("admin@company.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            logger.info("Default admin user created:");
            logger.info("Email: admin@company.com");
            logger.info("Password: admin123");
            logger.warn("Please change the default password after first login!");
        } else {
            logger.info("Admin users already exist. Skipping default admin creation.");
        }
    }
}
