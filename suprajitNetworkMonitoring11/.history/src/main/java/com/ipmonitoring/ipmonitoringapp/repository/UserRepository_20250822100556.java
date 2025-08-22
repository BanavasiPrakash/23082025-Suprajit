package com.ipmonitoring.ipmonitoringapp.repository;

import com.ipmonitoring.ipmonitoringapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ipmonitoring.ipmonitoringapp.repository.UserRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
