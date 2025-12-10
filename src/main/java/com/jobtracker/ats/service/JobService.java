package com.jobtracker.ats.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.jobtracker.ats.dto.JobRequest;
import com.jobtracker.ats.entity.Job;
import com.jobtracker.ats.entity.User;
import com.jobtracker.ats.repository.JobRepository;

@Service
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userDetailsService.getUserByEmail(email);
    }

    // Method to be added to CustomUserDetailsService temporarily

    public Job createJob(JobRequest request) {
        User recruiter = getCurrentUser();
        
        if(recruiter.getRole().name().equals("CANDIDATE")) {
            throw new RuntimeException("Candidates cannot post jobs");
        }

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRecruiter(recruiter);
        job.setCompanyName(recruiter.getCompanyName());

        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
    }
}
