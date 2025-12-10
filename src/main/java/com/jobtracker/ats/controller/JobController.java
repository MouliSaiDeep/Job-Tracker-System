package com.jobtracker.ats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobtracker.ats.dto.JobRequest;
import com.jobtracker.ats.entity.Job;
import com.jobtracker.ats.service.JobService;

@RestController
@RequestMapping("/jobs")
public class JobController {
    
    @Autowired
    private JobService jobService;

    //POST: Create a new job
    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody JobRequest request) {
        Job job = jobService.createJob(request);
        return ResponseEntity.ok(job);
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }
}
