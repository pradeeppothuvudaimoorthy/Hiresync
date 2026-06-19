package com.hiresync.service;

import com.hiresync.model.Recruiter;
import com.hiresync.model.User;
import com.hiresync.repository.RecruiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecruiterService {

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private EmailService emailService;

    public Optional<Recruiter> getRecruiterByUserId(Long userId) {
        return recruiterRepository.findByUserId(userId);
    }

    public Optional<Recruiter> getRecruiterById(Long id) {
        return recruiterRepository.findById(id);
    }

    public List<Recruiter> getAllRecruiters() {
        return recruiterRepository.findAll();
    }

    @Transactional
    public Recruiter registerRecruiter(User user, String companyName, String companyWebsite, String companyLocation, String phone) {
        Recruiter recruiter = new Recruiter(user, companyName, companyWebsite, companyLocation, phone);
        // By default recruiters are set to PENDING for admin approval. Let's send an email too.
        Recruiter saved = recruiterRepository.save(recruiter);
        emailService.sendEmail(
                user.getEmail(),
                "HireSync Recruiter Registration Successful",
                "Dear " + user.getName() + ",\n\nYour registration as a Recruiter on HireSync is successful. " +
                "Your account is currently PENDING review by the Administrator.\n\nRegards,\nHireSync Team"
        );
        return saved;
    }

    @Transactional
    public Recruiter updateProfile(Long recruiterId, String companyName, String companyWebsite, String companyLocation, String phone) {
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter not found with ID: " + recruiterId));

        recruiter.setCompanyName(companyName);
        recruiter.setCompanyWebsite(companyWebsite);
        recruiter.setCompanyLocation(companyLocation);
        recruiter.setPhone(phone);

        return recruiterRepository.save(recruiter);
    }

    @Transactional
    public void updateStatus(Long recruiterId, String status) {
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter not found with ID: " + recruiterId));

        String oldStatus = recruiter.getStatus();
        recruiter.setStatus(status);
        recruiterRepository.save(recruiter);

        // Send email notification upon approval or block
        String subject = "HireSync Account Status Updated";
        String body = "";
        if ("APPROVED".equalsIgnoreCase(status)) {
            body = "Dear Recruiter,\n\nYour account has been APPROVED. You can now login and post job openings.\n\nRegards,\nHireSync Team";
        } else if ("BLOCKED".equalsIgnoreCase(status)) {
            body = "Dear Recruiter,\n\nYour account has been BLOCKED. Please contact the administrator for details.\n\nRegards,\nHireSync Team";
        }
        
        if (!body.isEmpty()) {
            emailService.sendEmail(recruiter.getUser().getEmail(), subject, body);
        }
    }
}
