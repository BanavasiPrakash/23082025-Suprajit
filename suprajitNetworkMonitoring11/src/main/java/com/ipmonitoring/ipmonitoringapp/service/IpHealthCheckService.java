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

    @Scheduled(fixedDelay = 2000) // every 2 seconds
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

            // If status changed, update status change info
            if (previousStatus == null || !newStatus.equals(previousStatus)) {
                ipAddress.setStatusChangeCount(ipAddress.getStatusChangeCount() + 1);
                ipAddress.setLastStatusChangeStart(ipAddress.getLastStatusChangeEnd() != null ? ipAddress.getLastStatusChangeEnd() : now);
                ipAddress.setLastStatusChangeEnd(now);
                ipAddress.setStatus(newStatus);
            } else {
                ipAddress.setStatus(newStatus);
            }

            ipAddress.setLastChecked(now);
            repository.save(ipAddress);

            // Add to history if status changed or initial insert
            if (previousStatus == null || !newStatus.equals(previousStatus)) {
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
    }

    // Delete history older than 3 days, runs daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanOldStatusHistory() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).minusDays(3);
        historyRepository.deleteByCheckedAtBefore(cutoff);
    }
}
