package com.jobtracker.ats.repository;

import com.jobtracker.ats.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

}
