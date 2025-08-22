package com.ipmonitoring.ipmonitoringapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ipmonitoring.ipmonitoringapp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

