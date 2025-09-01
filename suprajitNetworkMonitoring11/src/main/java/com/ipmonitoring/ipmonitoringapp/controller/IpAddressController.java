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
import com.ipmonitoring.ipmonitoringapp.service.IpAddressService;

@CrossOrigin
@RestController
@RequestMapping("/api/ip")
public class IpAddressController {

    private final IpAddressRepository repo;
    private final UserRepository userRepository;
    private final IpAddressService ipAddressService;

    public IpAddressController(IpAddressRepository repo, UserRepository userRepository, IpAddressService ipAddressService) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.ipAddressService = ipAddressService;
    }

    @GetMapping
    public List<IpAddress> getAll() {
        return repo.findAll();
    }

    @GetMapping("/status-details")
    public List<IpStatusDetails> getAllWithStatusDetails() {
        return repo.findAll().stream()
                .map(ip -> {
                    Long id = ip.getId();
                    String location = ip.getLocation();
                    String ipaddr = ip.getIp();
                    String status = ip.getStatus();
                    int changeCount = ip.getStatusChangeCount();
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

    public static class IpStatusDetails {
        public Long id;
        public String location;
        public String ip;
        public String status;
        public int statusChangeCount;
        public String lastStatusDuration;

        public IpStatusDetails(Long id, String location, String ip, String status, int statusChangeCount, String lastStatusDuration) {
            this.id = id;
            this.location = location;
            this.ip = ip;
            this.status = status;
            this.statusChangeCount = statusChangeCount;
            this.lastStatusDuration = lastStatusDuration;
        }
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestParam String location, @RequestParam String ip,
                                 @RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("Forbidden: Only admin can add IP addresses");
        }

        if (repo.findByIp(ip).isPresent()) {
            return ResponseEntity.status(409).body("IP address already exists.");
        }

        IpAddress addr = new IpAddress();
        addr.setLocation(location);
        addr.setIp(ip);
        try {
            IpAddress saved = repo.save(addr);

            // Immediately perform health check and update status in DB
            ipAddressService.performImmediateHealthCheck(saved.getId());

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to add IP address.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIp(@PathVariable Long id, @RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("Forbidden: Only admin can delete IP addresses");
        }
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            ipAddressService.deleteIpAddressSafely(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete IP address.");
        }
    }

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
