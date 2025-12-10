package com.jobtracker.ats.repository;

import com.jobtracker.ats.entity.ApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {
    List<ApplicationHistory> findByApplicationId(Long applicationId);
}