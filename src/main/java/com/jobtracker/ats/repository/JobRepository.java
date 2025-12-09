package com.jobtracker.ats.repository;

import com.jobtracker.ats.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long>{
    // Queries will be added here later if required
}
