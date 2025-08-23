package com.ipmonitoring.ipmonitoringapp.service;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ipmonitoring.ipmonitoringapp.model.IpAddress;
import com.ipmonitoring.ipmonitoringapp.model.IpStatusHistory;
import com.ipmonitoring.ipmonitoringapp.repository.IpAddressRepository;
import com.ipmonitoring.ipmonitoringapp.repository.IpStatusHistoryRepository;

@Service
public class IpHealthCheckService {

    private final IpAddressRepository repository;
    private final IpStatusHistoryRepository historyRepository;

    public IpHealthCheckService(IpAddressRepository repository, IpStatusHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    @Scheduled(fixedDelay = 2000) // every 2 seconds
    public void healthCheckAllIps() {
        List<IpAddress> ips = repository.findAll();
        for (IpAddress ipAddress : ips) {
            String previousStatus = ipAddress.getStatus();
            LocalDateTime now = LocalDateTime.now();

            try {
                InetAddress inet = InetAddress.getByName(ipAddress.getIp());
                boolean reachable = inet.isReachable(2000); // 2s timeout
                String newStatus = reachable ? "Online" : "Down";

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

            // Save a record to history only when status changes
            if (!ipAddress.getStatus().equals(previousStatus)) {
                IpStatusHistory history = new IpStatusHistory();
                history.setIpId(ipAddress.getId());
                history.setLocation(ipAddress.getLocation());
                history.setIp(ipAddress.getIp());
                history.setStatus(ipAddress.getStatus());
                history.setCheckedAt(now);
                history.setStatusChangeCount(ipAddress.getStatusChangeCount());

                if (ipAddress.getLastStatusChangeStart() != null && ipAddress.getLastStatusChangeEnd() != null) {
                    long duration = Duration.between(ipAddress.getLastStatusChangeStart(), ipAddress.getLastStatusChangeEnd()).getSeconds();
                    history.setDurationSeconds(duration);
                } else {
                    history.setDurationSeconds(0L);
                }

                historyRepository.save(history);
            }
        }
    }

    // Scheduled task for cleaning up old history records older than 3 days
    @Scheduled(cron = "0 0 0 * * ?") // at midnight every day
    public void cleanOldStatusHistory() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        historyRepository.deleteByCheckedAtBefore(cutoff);
    }
}
