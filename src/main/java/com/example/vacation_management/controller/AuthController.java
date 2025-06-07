package com.example.vacation_management.controller;


import com.example.vacation_management.dto.LoginRequestDto;
import com.example.vacation_management.dto.LoginResponseDto;
import com.example.vacation_management.dto.UserDto;
import com.example.vacation_management.entity.User;
import com.example.vacation_management.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());
        LoginResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> register(@Valid @RequestBody UserDto userDto) {
        logger.info("Registration attempt for email: {}", userDto.getEmail());
        User user = authService.register(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> passwordData,
            HttpServletRequest request) {

        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            throw new RuntimeException("Old password and new password are required");
        }

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        authService.changePassword(userId, oldPassword, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);

        User user = authService.getUserFromToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("role", user.getRole());
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

}
