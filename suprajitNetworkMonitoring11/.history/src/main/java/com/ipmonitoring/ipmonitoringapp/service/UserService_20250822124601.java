package com.ipmonitoring.ipmonitoringapp.service;

import com.ipmonitoring.ipmonitoringapp.model.User;
import com.ipmonitoring.ipmonitoringapp.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("USER"); // Default to USER role
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.orElse(null);
    }

    // New method to find user by email for password reset
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // New method to save user, for example after password reset
    public void save(User user) {
        userRepository.save(user);
    }
}
