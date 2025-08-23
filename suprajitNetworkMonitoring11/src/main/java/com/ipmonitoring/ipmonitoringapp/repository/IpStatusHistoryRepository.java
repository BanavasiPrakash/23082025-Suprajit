
package com.ipmonitoring.ipmonitoringapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ipmonitoring.ipmonitoringapp.model.IpStatusHistory;

public interface IpStatusHistoryRepository extends JpaRepository<IpStatusHistory, Long> {

    List<IpStatusHistory> findByCheckedAtAfter(LocalDateTime cutoff);

    void deleteByCheckedAtBefore(LocalDateTime cutoff);
}
