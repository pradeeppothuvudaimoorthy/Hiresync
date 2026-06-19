package com.hiresync.service;

import com.hiresync.model.*;
import com.hiresync.repository.ApplicationRepository;
import com.hiresync.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    public Optional<Interview> getInterviewById(Long id) {
        return interviewRepository.findById(id);
    }

    public Optional<Interview> getInterviewByApplicationId(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId);
    }

    public List<Interview> getInterviewsForCandidate(Long candidateId) {
        return interviewRepository.findByApplicationCandidateId(candidateId);
    }

    public List<Interview> getInterviewsForRecruiter(Long recruiterId) {
        return interviewRepository.findByApplicationJobRecruiterId(recruiterId);
    }

    @Transactional
    public Interview scheduleInterview(Long applicationId, LocalDate date, LocalTime time, String mode, String meetingLink, String location, Recruiter recruiter) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        // Security check: Recruiter must own this job
        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You do not have permission to schedule interviews for this application.");
        }

        // Validation 1: Interview date cannot be in the past
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Interview date cannot be in the past.");
        }

        // Validation 2: Application status must be Shortlisted or Interview Scheduled
        String status = application.getStatus();
        if (!"Shortlisted".equalsIgnoreCase(status) && !"Interview Scheduled".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Interviews can only be scheduled for shortlisted applications.");
        }

        // Create or update interview
        Optional<Interview> existing = interviewRepository.findByApplicationId(applicationId);
        Interview interview;
        if (existing.isPresent()) {
            interview = existing.get();
        } else {
            interview = new Interview();
            interview.setApplication(application);
        }

        interview.setInterviewDate(date);
        interview.setInterviewTime(time);
        interview.setMode(mode);
        interview.setMeetingLink(meetingLink);
        interview.setLocation(location);
        interview.setStatus("Scheduled");

        // Save interview and update application status
        Interview saved = interviewRepository.save(interview);
        application.setStatus("Interview Scheduled");
        applicationRepository.save(application);

        // Notify Candidate (Email & App Notification)
        User candidateUser = application.getCandidate().getUser();
        String candidateMsg = "Interview scheduled for " + application.getJob().getTitle() + " on " + date + " at " + time + " (" + mode + ").";
        notificationService.sendNotification(candidateUser, candidateMsg);

        String details = "Date: " + date + "\n" +
                         "Time: " + time + "\n" +
                         "Mode: " + mode + "\n";
        if ("Online".equalsIgnoreCase(mode)) {
            details += "Meeting Link: " + meetingLink + "\n";
        } else {
            details += "Location: " + location + "\n";
        }

        emailService.sendEmail(
                candidateUser.getEmail(),
                "HireSync - Interview Scheduled",
                "Dear " + candidateUser.getName() + ",\n\n" +
                "An interview has been scheduled for your application for the role of " + application.getJob().getTitle() + " at " + application.getJob().getCompanyName() + ".\n\n" +
                "Details:\n" + details + "\n" +
                "Please check your candidate dashboard for complete information.\n\n" +
                "Regards,\nHireSync Team"
        );

        return saved;
    }

    @Transactional
    public void cancelInterview(Long interviewId, Recruiter recruiter) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found with ID: " + interviewId));

        Application application = interview.getApplication();

        // Security check
        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You do not have permission to cancel this interview.");
        }

        // Update status
        interview.setStatus("Cancelled");
        interviewRepository.save(interview);

        application.setStatus("Shortlisted"); // Reverts application status back to shortlisted
        applicationRepository.save(application);

        // Notify Candidate
        User candidateUser = application.getCandidate().getUser();
        String candidateMsg = "Your interview for " + application.getJob().getTitle() + " has been CANCELLED.";
        notificationService.sendNotification(candidateUser, candidateMsg);

        emailService.sendEmail(
                candidateUser.getEmail(),
                "HireSync - Interview Cancelled",
                "Dear " + candidateUser.getName() + ",\n\n" +
                "The interview scheduled for the role of " + application.getJob().getTitle() + " has been cancelled by the recruiter.\n\n" +
                "Your application remains in the Shortlisted state. Check your dashboard for updates.\n\n" +
                "Regards,\nHireSync Team"
        );
    }
}
