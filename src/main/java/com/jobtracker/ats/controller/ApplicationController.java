package com.jobtracker.ats.controller;

import com.jobtracker.ats.dto.StatusUpdateRequest;
import com.jobtracker.ats.entity.Application;
import com.jobtracker.ats.entity.ApplicationHistory;
import com.jobtracker.ats.service.ApplicationService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // POST: Candidate applies for a job (Existing method)
    @PostMapping("/{jobId}")
    public ResponseEntity<Application> applyForJob(@PathVariable Long jobId) {
        Application application = applicationService.applyForJob(jobId);
        return ResponseEntity.ok(application);
    }

    // PATCH: Recruiter updates application status
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<Application> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody StatusUpdateRequest request) {

        Application updatedApplication = applicationService.updateApplicationStatus(applicationId, request);
        return ResponseEntity.ok(updatedApplication);
    }

    // GET: Recruiter views all applications for a job
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getApplicationsForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId));
    }

    // GET: Candidate views their own applications
    @GetMapping("/my-applications")
    public ResponseEntity<List<Application>> getMyApplications() {
        return ResponseEntity.ok(applicationService.getMyApplications());
    }

    // GET: View History (Updated logic from previous step)
    @GetMapping("/{applicationId}/history")
    public ResponseEntity<List<ApplicationHistory>> getApplicationHistory(@PathVariable Long applicationId) {
        return ResponseEntity.ok(applicationService.getApplicationHistory(applicationId));
    }
}