package com.ipmonitoring.ipmonitoringapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ipmonitoring.ipmonitoringapp.model.IpAddress;
import com.ipmonitoring.ipmonitoringapp.model.User;
import com.ipmonitoring.ipmonitoringapp.repository.IpAddressRepository;
import com.ipmonitoring.ipmonitoringapp.repository.UserRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/ip")
public class IpAddressController {

    private final IpAddressRepository repo;
    private final UserRepository userRepository; // Added

    public IpAddressController(IpAddressRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    // Get all IP addresses (accessible to all logged-in users)
    @GetMapping
    public List<IpAddress> getAll() {
        return repo.findAll();
    }

    // Add new IP address (admin only)
    @PostMapping
    public ResponseEntity<?> add(@RequestParam String location, @RequestParam String ip,
            @RequestParam String username) {
        User user = userRepository.findById(username).orElse(null);
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
        User user = userRepository.findById(username).orElse(null);
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
        User user = userRepository.findById(username).orElse(null);
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
