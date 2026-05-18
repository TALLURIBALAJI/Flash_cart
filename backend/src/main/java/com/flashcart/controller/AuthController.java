package com.flashcart.controller;

import com.flashcart.config.JwtUtil;
import com.flashcart.entities.User;
import com.flashcart.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Auth Controller - JWT-based authentication endpoints (public)
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8000", "https://flashcart-frontend.onrender.com"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
            );

            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", mapUserToResponse(user));
            response.put("message", "Registration successful");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Login with email and password
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.getUserByEmail(request.getEmail());

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account is deactivated"));
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", mapUserToResponse(user));
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    /**
     * Validate current token and return user info
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestAttribute("userEmail") String email) {
        try {
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(mapUserToResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }
    }

    private Map<String, Object> mapUserToResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("currency", user.getCurrency());
        map.put("avatarUrl", user.getAvatarUrl());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }

    // Request DTOs
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 2, max = 100)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        public RegisterRequest() {}
        public String getUsername() {return username;}
        public void setUsername(String username) {this.username = username;}
        public String getEmail() {return email;}
        public void setEmail(String email) {this.email = email;}
        public String getPassword() {return password;}
        public void setPassword(String password) {this.password = password;}
    }

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}
        public String getEmail() {return email;}
        public void setEmail(String email) {this.email = email;}
        public String getPassword() {return password;}
        public void setPassword(String password) {this.password = password;}
    }
}
