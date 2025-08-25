
package com.ipmonitoring.ipmonitoringapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ip_status_history")
public class IpStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_id", nullable = false)
    private Long ipId;

    private String location;

    private String ip;

    private String status;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(name = "status_change_count")
    private int statusChangeCount;

    // @Column(name = "duration_seconds")
    // private Long durationSeconds;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIpId() { return ipId; }
    public void setIpId(Long ipId) { this.ipId = ipId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }

    public int getStatusChangeCount() { return statusChangeCount; }
    public void setStatusChangeCount(int statusChangeCount) { this.statusChangeCount = statusChangeCount; }

    // public Long getDurationSeconds() { return durationSeconds; }
    // public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
}
