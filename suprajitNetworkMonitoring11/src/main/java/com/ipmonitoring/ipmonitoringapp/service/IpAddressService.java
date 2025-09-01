package com.ipmonitoring.ipmonitoringapp.service;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipmonitoring.ipmonitoringapp.model.IpAddress;
import com.ipmonitoring.ipmonitoringapp.model.IpStatusHistory;
import com.ipmonitoring.ipmonitoringapp.repository.IpAddressRepository;
import com.ipmonitoring.ipmonitoringapp.repository.IpStatusHistoryRepository;

@Service
public class IpAddressService {

    private final IpAddressRepository ipAddressRepository;
    private final IpStatusHistoryRepository ipStatusHistoryRepository;

    public IpAddressService(IpAddressRepository ipAddressRepository,
                            IpStatusHistoryRepository ipStatusHistoryRepository) {
        this.ipAddressRepository = ipAddressRepository;
        this.ipStatusHistoryRepository = ipStatusHistoryRepository;
    }

    @Transactional
    public void deleteIpAddressSafely(Long ipId) {
        // Delete related history rows first
        ipStatusHistoryRepository.deleteByIpId(ipId);

        // Then delete IP record
        ipAddressRepository.deleteById(ipId);
    }

    @Transactional
    public void updateIpStatusAndAddHistory(Long ipId, String currentStatus) {
        Optional<IpAddress> optionalIp = ipAddressRepository.findById(ipId);
        if (!optionalIp.isPresent()) {
            return; // Or throw exception
        }

        IpAddress ip = optionalIp.get();

        IpStatusHistory lastHistory = ipStatusHistoryRepository.findTopByIpIdOrderByCheckedAtDesc(ipId);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        if (lastHistory == null) {
            createHistory(ip, currentStatus, 1, now);
            return;
        }

        boolean statusChanged = !lastHistory.getStatus().equals(currentStatus);
        boolean timeElapsed = Duration.between(lastHistory.getCheckedAt(), now).toMinutes() >= 1;

        if (statusChanged || timeElapsed) {
            int newCount = statusChanged ? 1 : lastHistory.getStatusChangeCount() + 1;
            createHistory(ip, currentStatus, newCount, now);
        }
    }

    private void createHistory(IpAddress ip, String status, int count, LocalDateTime checkedAt) {
        IpStatusHistory history = new IpStatusHistory();
        history.setIpId(ip.getId());
        history.setLocation(ip.getLocation());
        history.setIp(ip.getIp());
        history.setStatus(status);
        history.setStatusChangeCount(count);
        history.setCheckedAt(checkedAt);
        ipStatusHistoryRepository.save(history);
    }

    @Transactional
    public void performImmediateHealthCheck(Long ipId) {
        Optional<IpAddress> optionalIp = ipAddressRepository.findById(ipId);
        if (!optionalIp.isPresent()) {
            return;
        }

        IpAddress ipAddress = optionalIp.get();
        String newStatus = "Error";
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        try {
            InetAddress inet = InetAddress.getByName(ipAddress.getIp());
            boolean reachable = inet.isReachable(2000); // 2 second timeout
            newStatus = reachable ? "Online" : "Down";
        } catch (Exception e) {
            newStatus = "Error";
        }

        ipAddress.setStatus(newStatus);
        ipAddress.setLastChecked(now);
        ipAddress.setLastStatusChangeStart(now);
        ipAddress.setLastStatusChangeEnd(now);
        ipAddress.setStatusChangeCount(1);

        ipAddressRepository.save(ipAddress);

        // Save history record
        IpStatusHistory history = new IpStatusHistory();
        history.setIpId(ipAddress.getId());
        history.setLocation(ipAddress.getLocation());
        history.setIp(ipAddress.getIp());
        history.setStatus(newStatus);
        history.setCheckedAt(now);
        history.setStatusChangeCount(1);

        ipStatusHistoryRepository.save(history);
    }
}
