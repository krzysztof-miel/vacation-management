package com.example.vacation_management.service;


import com.example.vacation_management.dto.LoginRequestDto;
import com.example.vacation_management.dto.LoginResponseDto;
import com.example.vacation_management.dto.UserDto;
import com.example.vacation_management.entity.Role;
import com.example.vacation_management.entity.User;
import com.example.vacation_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        logger.info("Attempting login for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", loginRequest.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.error("Invalid password for user: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        logger.info("User {} logged in successfully", loginRequest.getEmail());

        return new LoginResponseDto(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    public User register(UserDto userDto) {
        logger.info("Attempting to register new user with email: {}", userDto.getEmail());

        if (userRepository.existsByEmail(userDto.getEmail())) {
            logger.error("Email already exists: {}", userDto.getEmail());
            throw new RuntimeException("User with email " + userDto.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(userDto.getRole() != null ? userDto.getRole() : Role.EMPLOYEE);

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.EMPLOYEE) {
            try {
                int currentYear = LocalDate.now().getYear();
                leaveBalanceService.createDefaultBalanceForUser(savedUser, currentYear, 26); // 26 dni urlopu domyślnie
                logger.info("Created default leave balance for new user: {}", savedUser.getEmail());
            } catch (Exception e) {
                logger.warn("Failed to create leave balance for new user: {} - {}", savedUser.getEmail(), e.getMessage());
            }
        }

        logger.info("User registered successfully: {}", savedUser.getEmail());
        return savedUser;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        logger.info("Attempting to change password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.error("Invalid old password for user ID: {}", userId);
            throw new RuntimeException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully for user ID: {}", userId);
    }

    public User getUserFromToken(String token) {
        try {
            String email = jwtService.extractEmail(token);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            logger.error("Failed to get user from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

}
