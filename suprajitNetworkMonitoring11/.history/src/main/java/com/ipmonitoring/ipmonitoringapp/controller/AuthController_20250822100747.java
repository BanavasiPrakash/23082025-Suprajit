package com.ipmonitoring.ipmonitoringapp.controller;

import com.ipmonitoring.ipmonitoringapp.model.User;
import com.ipmonitoring.ipmonitoringapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestParam String username, @RequestParam String password) {
        try {
            userService.signup(username, password);
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            if (auth.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                // Fetch user details with role
                User user = userRepository.findByUsername(username);

                if (user == null) {
                    return ResponseEntity.status(401).body("User not found");
                }
                // Return user info with role (do NOT include password)
                return ResponseEntity.ok(new LoginResponse(user.getUsername(), user.getRole()));
            }
        } catch (Exception e) {
            // Authentication failed
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    static class LoginResponse {
        private String username;
        private String role;

        public LoginResponse(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
