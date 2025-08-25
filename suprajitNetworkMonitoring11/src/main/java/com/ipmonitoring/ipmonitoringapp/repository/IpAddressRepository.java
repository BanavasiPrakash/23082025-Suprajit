package com.ipmonitoring.ipmonitoringapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ipmonitoring.ipmonitoringapp.model.IpAddress;

public interface IpAddressRepository extends JpaRepository<IpAddress, Long> {

    Optional<IpAddress> findByIp(String ip);
}
