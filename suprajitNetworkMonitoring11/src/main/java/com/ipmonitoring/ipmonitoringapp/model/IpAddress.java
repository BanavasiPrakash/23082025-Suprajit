package com.ipmonitoring.ipmonitoringapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ip_addresses")
public class IpAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private String ip;

    @Column(nullable = false)
    private String status = "Unknown";

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    // ====== NEW FIELDS for tracking status changes ======
    @Column(name = "status_change_count", nullable = false)
    private int statusChangeCount = 0;

    @Column(name = "last_status_change_start")
    private LocalDateTime lastStatusChangeStart;

    @Column(name = "last_status_change_end")
    private LocalDateTime lastStatusChangeEnd;

    // ========== Getters and Setters ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }

    // Getters and setters for new fields
    public int getStatusChangeCount() { return statusChangeCount; }
    public void setStatusChangeCount(int statusChangeCount) { this.statusChangeCount = statusChangeCount; }

    public LocalDateTime getLastStatusChangeStart() { return lastStatusChangeStart; }
    public void setLastStatusChangeStart(LocalDateTime lastStatusChangeStart) { this.lastStatusChangeStart = lastStatusChangeStart; }

    public LocalDateTime getLastStatusChangeEnd() { return lastStatusChangeEnd; }
    public void setLastStatusChangeEnd(LocalDateTime lastStatusChangeEnd) { this.lastStatusChangeEnd = lastStatusChangeEnd; }
}
