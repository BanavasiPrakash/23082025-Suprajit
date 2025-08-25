package com.ipmonitoring.ipmonitoringapp.service;

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
        // Delete all related history rows first
        ipStatusHistoryRepository.deleteByIpId(ipId);

        // Then delete the IP address record
        ipAddressRepository.deleteById(ipId);
    }

    // New method to check status and add history record if needed
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
}
