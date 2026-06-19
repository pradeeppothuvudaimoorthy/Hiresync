package com.hiresync.service;

import com.hiresync.entity.Resume;
import com.hiresync.model.*;
import com.hiresync.repository.ApplicationRepository;
import com.hiresync.repository.JobRepository;
import com.hiresync.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private JobMatchingService jobMatchingService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public List<Application> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    public List<Application> getApplicationsByRecruiter(Long recruiterId) {
        return applicationRepository.findByJobRecruiterId(recruiterId);
    }

    @Transactional
    public Application applyForJob(Candidate candidate, Long jobId) {
        // Validation 1: Candidate must have an active resume
        Resume activeResume = resumeRepository.findByCandidateIdAndActiveTrue(candidate.getId()).orElse(null);
        if (activeResume == null) {
            throw new IllegalArgumentException("You must upload a resume before applying for a job.");
        }

        // Validation 2: Cannot apply twice
        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
            throw new IllegalArgumentException("You have already applied for this job.");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        // Validation 3: Job must be ACTIVE
        if (!JobStatus.ACTIVE.name().equalsIgnoreCase(job.getStatus())) {
            throw new IllegalArgumentException("This job posting is closed.");
        }

        // Validation 4: Application deadline
        if (job.getLastDateToApply() != null && LocalDate.now().isAfter(job.getLastDateToApply())) {
            throw new IllegalArgumentException("The application deadline for this job has passed.");
        }

        // Create application
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setResume(activeResume);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        // Advanced match score calculation
        int matchScore = jobMatchingService.calculateMatchScore(candidate, job);
        application.setMatchScore((double) matchScore);
        
        MatchStatus matchStatus;
        if (matchScore >= 80) {
            matchStatus = MatchStatus.HIGH_MATCH;
        } else if (matchScore >= 50) {
            matchStatus = MatchStatus.MEDIUM_MATCH;
        } else {
            matchStatus = MatchStatus.LOW_MATCH;
        }
        application.setMatchStatusEnum(matchStatus);

        Application saved = applicationRepository.save(application);

        // Increment applicant count on job
        job.setApplicantCount(job.getApplicantCount() + 1);
        jobRepository.save(job);

        // Notify Recruiter (Email and App Notification)
        Recruiter recruiter = job.getRecruiter();
        String recruiterMsg = "New application received from candidate " + candidate.getUser().getName() + " for your job posting: " + job.getTitle();
        notificationService.sendNotification(recruiter.getUser(), recruiterMsg);
        emailService.sendEmail(
                recruiter.getUser().getEmail(),
                "HireSync - New Application Received",
                "Dear " + recruiter.getUser().getName() + ",\n\n" +
                "You have received a new application for the role of " + job.getTitle() + ".\n\n" +
                "Applicant: " + candidate.getUser().getName() + "\n" +
                "Match Score: " + matchScore + "% (" + matchStatus.getDisplayName() + ")\n\n" +
                "Please check your recruiter dashboard to review the application and resume.\n\n" +
                "Regards,\nHireSync Team"
        );

        // Notify Candidate (Email and App Notification)
        String candidateMsg = "You successfully applied for " + job.getTitle() + " at " + job.getCompanyName();
        notificationService.sendNotification(candidate.getUser(), candidateMsg);
        emailService.sendEmail(
                candidate.getUser().getEmail(),
                "HireSync - Application Submitted Successfully",
                "Dear " + candidate.getUser().getName() + ",\n\n" +
                "Your application for the role of " + job.getTitle() + " at " + job.getCompanyName() + " has been submitted successfully.\n\n" +
                "Status: Applied\n" +
                "We will notify you of further updates.\n\n" +
                "Regards,\nHireSync Team"
        );

        return saved;
    }

    @Transactional
    public Application updateApplicationStatus(Long applicationId, String status, Recruiter recruiter) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        // Security check: Only the recruiter who posted the job can update the application status
        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You do not have permission to manage this application.");
        }

        application.setStatus(status);
        Application saved = applicationRepository.save(application);

        // Notify Candidate (Email & App Notification)
        User candidateUser = application.getCandidate().getUser();
        String candidateMsg = "Your application status for " + application.getJob().getTitle() + " at " + application.getJob().getCompanyName() + " has been updated to: " + status;
        notificationService.sendNotification(candidateUser, candidateMsg);

        // Prepare email template body based on status
        String subject = "HireSync - Application Status Update";
        String body = "Dear " + candidateUser.getName() + ",\n\n" +
                "Your application for the role of " + application.getJob().getTitle() + " has been updated to: " + status + ".\n\n";

        if ("Shortlisted".equalsIgnoreCase(status)) {
            body += "Congratulations! You have been shortlisted for this role. The recruiter will schedule an interview round shortly. Please keep checking your dashboard.\n\n";
        } else if ("Rejected".equalsIgnoreCase(status)) {
            body += "Thank you for your interest in this role. Unfortunately, the recruiter decided not to move forward with your application at this time.\n\n";
        } else if ("Selected".equalsIgnoreCase(status)) {
            body += "Congratulations! You have been selected for this position. The HR team will contact you shortly with the onboarding details.\n\n";
        } else {
            body += "Please check your HireSync dashboard for more details.\n\n";
        }

        body += "Regards,\nHireSync Team";

        emailService.sendEmail(candidateUser.getEmail(), subject, body);

        return saved;
    }
}
