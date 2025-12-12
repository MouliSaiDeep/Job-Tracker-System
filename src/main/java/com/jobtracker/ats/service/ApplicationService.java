package com.jobtracker.ats.service;

import com.jobtracker.ats.dto.StatusUpdateRequest;
import com.jobtracker.ats.dto.NotificationMessage;
import com.jobtracker.ats.entity.*;
import com.jobtracker.ats.repository.ApplicationHistoryRepository;
import com.jobtracker.ats.repository.ApplicationRepository;
import com.jobtracker.ats.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private ApplicationHistoryRepository historyRepository;

    @Autowired
    private RabbitMQSender rabbitMQSender; // <--- NEW INJECTION

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> VALID_TRANSITIONS = Map.of(
        ApplicationStatus.APPLIED, Set.of(ApplicationStatus.SCREENING, ApplicationStatus.REJECTED),
        ApplicationStatus.SCREENING, Set.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED),
        ApplicationStatus.INTERVIEW, Set.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED),
        ApplicationStatus.OFFER, Set.of(ApplicationStatus.HIRED, ApplicationStatus.REJECTED),
        ApplicationStatus.HIRED, Set.of(), 
        ApplicationStatus.REJECTED, Set.of() 
    );

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userDetailsService.getUserByEmail(email);
    }
    
    private boolean isValidTransition(ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        if (oldStatus == ApplicationStatus.HIRED || oldStatus == ApplicationStatus.REJECTED) {
            return false;
        }
        Set<ApplicationStatus> allowed = VALID_TRANSITIONS.getOrDefault(oldStatus, Set.of());
        return allowed.contains(newStatus);
    }
    
    public Application applyForJob(Long jobId) {
        User candidate = getCurrentUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!candidate.getRole().equals(Role.CANDIDATE)) {
            throw new RuntimeException("Only CANDIDATES can apply for a job.");
        }
        
        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        
        Application savedApplication = applicationRepository.save(application);
        
        ApplicationHistory history = new ApplicationHistory();
        history.setApplication(savedApplication);
        history.setOldStatus(null);
        history.setNewStatus(ApplicationStatus.APPLIED);
        history.setUpdatedBy(candidate);
        historyRepository.save(history);
        
        // --- ASYNC NOTIFICATION ---
        NotificationMessage msg = new NotificationMessage(
            candidate.getEmail(),
            "Application Received",
            "You have successfully applied for: " + job.getTitle()
        );
        rabbitMQSender.sendNotification(msg);
        
        return savedApplication;
    }

    @Transactional
    public Application updateApplicationStatus(Long applicationId, StatusUpdateRequest request) {
        User updater = getCurrentUser();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (updater.getRole().equals(Role.CANDIDATE)) {
            throw new RuntimeException("Permission denied. Only Recruiters/Managers can update application status.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus newStatus = request.getNewStatus();
        
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new RuntimeException(String.format("Invalid transition: Cannot move from %s to %s", oldStatus, newStatus));
        }

        application.setStatus(newStatus);
        Application updatedApplication = applicationRepository.save(application);

        ApplicationHistory history = new ApplicationHistory();
        history.setApplication(updatedApplication);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setUpdatedBy(updater);
        historyRepository.save(history);
        
        // --- ASYNC NOTIFICATION ---
        NotificationMessage msg = new NotificationMessage(
            application.getCandidate().getEmail(),
            "Application Status Update",
            "Your application status has changed to: " + newStatus
        );
        rabbitMQSender.sendNotification(msg);
        
        return updatedApplication;
    }

    // View History
    public List<ApplicationHistory> getApplicationHistory(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        User currentUser = getCurrentUser();
        boolean isOwner = application.getCandidate().getId().equals(currentUser.getId());
        boolean isRecruiter = currentUser.getRole() == Role.RECRUITER || currentUser.getRole() == Role.HIRING_MANAGER;
        if (!isOwner && !isRecruiter) {
            throw new RuntimeException("Access Denied: You cannot view this application's history.");
        }
        return historyRepository.findByApplicationId(applicationId);
    }

    // Recruiter View
    public List<Application> getApplicationsForJob(Long jobId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CANDIDATE) {
            throw new RuntimeException("Candidates cannot view other applicants.");
        }
        return applicationRepository.findByJobId(jobId);
    }

    // Candidate View
    public List<Application> getMyApplications() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.CANDIDATE) {
            throw new RuntimeException("Only candidates have a 'my applications' list.");
        }
        return applicationRepository.findByCandidateId(currentUser.getId());
    }
}