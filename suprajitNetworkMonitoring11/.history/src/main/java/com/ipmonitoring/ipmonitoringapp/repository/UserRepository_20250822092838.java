package com.ipmonitoring.ipmonitoringapp.repository;

import com.ipmonitoring.ipmonitoringapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
