package com.ipmonitoring.ipmonitoringapp.controller;

import com.ipmonitoring.ipmonitoringapp.model.PasswordResetToken;
import com.ipmonitoring.ipmonitoringapp.model.User;
import com.ipmonitoring.ipmonitoringapp.repository.PasswordResetTokenRepository;
import com.ipmonitoring.ipmonitoringapp.service.EmailService;
import com.ipmonitoring.ipmonitoringapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          EmailService emailService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
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
                User user = userService.findByUsername(username);

                if (user == null) {
                    return ResponseEntity.status(401).body("User not found");
                }
                return ResponseEntity.ok(new LoginResponse(user.getUsername(), user.getRole()));
            }
        } catch (Exception e) {
            // Authentication failed
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    // New endpoint to request password reset email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not registered");
        }
        User user = userOpt.get();

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(expiry);

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
        return ResponseEntity.ok("Password reset email sent");
    }

    // New endpoint to reset password with token
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
        User user = tokenOpt.get().getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);  // Use UserService save method here

        passwordResetTokenRepository.delete(tokenOpt.get());

        return ResponseEntity.ok("Password reset successful");
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
