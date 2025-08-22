package com.ipmonitoring.ipmonitoringapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ipmonitoring.ipmonitoringapp.model.IpAddress;
import com.ipmonitoring.ipmonitoringapp.repository.IpAddressRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/ip")
public class IpAddressController {

    private final IpAddressRepository repo;

    public IpAddressController(IpAddressRepository repo) {
        this.repo = repo;
    }

    // Get all IP addresses
    @GetMapping
    public List<IpAddress> getAll() {
        return repo.findAll();
    }

    // Add new IP address
    @PostMapping
    public ResponseEntity<IpAddress> add(@RequestParam String location, @RequestParam String ip) {
        IpAddress addr = new IpAddress();
        addr.setLocation(location);
        addr.setIp(ip);
        IpAddress saved = repo.save(addr);
        return ResponseEntity.ok(saved);
    }

    // Delete IP address by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIp(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Update IP address by id
    @PutMapping("/{id}")
public ResponseEntity<IpAddress> updateIp(
        @PathVariable Long id,
        @RequestParam String location,
        @RequestParam String ip) {
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
