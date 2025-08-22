package com.ipmonitoring.ipmonitoringapp.service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.ipmonitoring.ipmonitoringapp.model.IpAddress;
import com.ipmonitoring.ipmonitoringapp.repository.IpAddressRepository;

@Service
public class IpHealthCheckService {
    private final IpAddressRepository repository;

    public IpHealthCheckService(IpAddressRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 30000) // every 30 seconds
    public void healthCheckAllIps() {
        List<IpAddress> ips = repository.findAll();
        for (IpAddress ipAddress : ips) {
            String previousStatus = ipAddress.getStatus();
            LocalDateTime now = LocalDateTime.now();

            try {
                InetAddress inet = InetAddress.getByName(ipAddress.getIp());
                boolean reachable = inet.isReachable(2000); // 2s timeout
                String newStatus = reachable ? "Online" : "Down";

                // If status changed, update change count and timestamps
                if (!newStatus.equals(previousStatus)) {
                    ipAddress.setStatusChangeCount(ipAddress.getStatusChangeCount() + 1);
                    ipAddress.setLastStatusChangeEnd(now);
                    ipAddress.setLastStatusChangeStart(now);
                }
                ipAddress.setStatus(newStatus);
            } catch (Exception e) {
                if (!"Error".equals(previousStatus)) {
                    ipAddress.setStatusChangeCount(ipAddress.getStatusChangeCount() + 1);
                    ipAddress.setLastStatusChangeEnd(now);
                    ipAddress.setLastStatusChangeStart(now);
                }
                ipAddress.setStatus("Error");
            }

            ipAddress.setLastChecked(now);
            repository.save(ipAddress);
        }
    }
}
