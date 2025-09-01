package com.ipmonitoring.ipmonitoringapp.service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Scheduled(fixedDelay = 600000) // every 10 minutes (600,000 ms)
    public void healthCheckAllIps() {
        List<IpAddress> ips = repository.findAll();
        for (IpAddress ipAddress : ips) {
            String previousStatus = ipAddress.getStatus();
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            String newStatus = "Error";

            try {
                InetAddress inet = InetAddress.getByName(ipAddress.getIp());
                boolean reachable = inet.isReachable(2000); // 2s timeout
                newStatus = reachable ? "Online" : "Down";
            } catch (Exception e) {
                newStatus = "Error";
            }

            // Update last checked time
            ipAddress.setLastChecked(now);

            if (previousStatus == null || !newStatus.equals(previousStatus)) {
                ipAddress.setStatusChangeCount(ipAddress.getStatusChangeCount() + 1);
                ipAddress.setLastStatusChangeStart(ipAddress.getLastStatusChangeEnd() != null ? ipAddress.getLastStatusChangeEnd() : now);
                ipAddress.setLastStatusChangeEnd(now);
                ipAddress.setStatus(newStatus);
            } else {
                ipAddress.setStatus(newStatus);
            }

            repository.save(ipAddress);

            // Always save history record every 10 minutes regardless of status change
            IpStatusHistory history = new IpStatusHistory();
            history.setIpId(ipAddress.getId());
            history.setLocation(ipAddress.getLocation());
            history.setIp(ipAddress.getIp());
            history.setStatus(newStatus);
            history.setCheckedAt(now);
            history.setStatusChangeCount(ipAddress.getStatusChangeCount());

            historyRepository.save(history);
        }
    }

    // Scheduled task to clean up old status history records
    @Scheduled(cron = "0 0 0 * * ?") // runs daily at midnight
    public void cleanOldStatusHistory() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).minusDays(3);
        historyRepository.deleteByCheckedAtBefore(cutoff);
    }
}
