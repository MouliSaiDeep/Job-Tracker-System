package com.jobtracker.ats.dto;

import com.jobtracker.ats.entity.ApplicationStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private ApplicationStatus newStatus;
}