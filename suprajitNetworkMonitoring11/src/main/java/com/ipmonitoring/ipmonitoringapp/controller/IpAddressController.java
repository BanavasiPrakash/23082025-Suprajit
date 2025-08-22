package com.ipmonitoring.ipmonitoringapp.controller;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ipmonitoring.ipmonitoringapp.model.IpAddress;
import com.ipmonitoring.ipmonitoringapp.model.User;
import com.ipmonitoring.ipmonitoringapp.repository.IpAddressRepository;
import com.ipmonitoring.ipmonitoringapp.repository.UserRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/ip")
public class IpAddressController {

    private final IpAddressRepository repo;
    private final UserRepository userRepository;

    public IpAddressController(IpAddressRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    // Get all IP addresses (accessible to all logged-in users)
    @GetMapping
    public List<IpAddress> getAll() {
        return repo.findAll();
    }

    // ========== NEW: Get all IPs with status change count and duration ==========
    @GetMapping("/status-details")
    public List<IpStatusDetails> getAllWithStatusDetails() {
        return repo.findAll().stream()
                .map(ip -> {
                    Long id = ip.getId();
                    String location = ip.getLocation();
                    String ipaddr = ip.getIp();
                    String status = ip.getStatus();
                    int changeCount = ip.getStatusChangeCount();
                    // Duration value (in seconds or formatted HH:mm:ss)
                    Long durationSeconds = null;
                    String formattedDuration = "";
                    if (ip.getLastStatusChangeStart() != null && ip.getLastStatusChangeEnd() != null) {
                        durationSeconds = Duration.between(ip.getLastStatusChangeStart(), ip.getLastStatusChangeEnd()).getSeconds();
                        long hrs = durationSeconds / 3600;
                        long mins = (durationSeconds % 3600) / 60;
                        long secs = durationSeconds % 60;
                        formattedDuration = String.format("%02d:%02d:%02d", hrs, mins, secs);
                    }
                    return new IpStatusDetails(id, location, ipaddr, status, changeCount, formattedDuration);
                })
                .collect(Collectors.toList());
    }

    // DTO for status details:
    public static class IpStatusDetails {
        public Long id;
        public String location;
        public String ip;
        public String status;
        public int statusChangeCount;
        public String lastStatusDuration; // format: HH:mm:ss

        public IpStatusDetails(Long id, String location, String ip, String status, int statusChangeCount, String lastStatusDuration) {
            this.id = id;
            this.location = location;
            this.ip = ip;
            this.status = status;
            this.statusChangeCount = statusChangeCount;
            this.lastStatusDuration = lastStatusDuration;
        }
    }

    // Add new IP address (admin only)
    @PostMapping
    public ResponseEntity<?> add(@RequestParam String location, @RequestParam String ip,
                                 @RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("Forbidden: Only admin can add IP addresses");
        }
        IpAddress addr = new IpAddress();
        addr.setLocation(location);
        addr.setIp(ip);
        IpAddress saved = repo.save(addr);
        return ResponseEntity.ok(saved);
    }

    // Delete IP address by id (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIp(@PathVariable Long id, @RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("Forbidden: Only admin can delete IP addresses");
        }
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Update IP address by id (admin only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIp(
            @PathVariable Long id,
            @RequestParam String location,
            @RequestParam String ip,
            @RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("Forbidden: Only admin can update IP addresses");
        }
        return repo.findById(id)
                .map(existing -> {
                    existing.setLocation(location);
                    existing.setIp(ip);
                    IpAddress updated = repo.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
