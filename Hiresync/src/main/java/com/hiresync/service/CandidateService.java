package com.hiresync.service;

import com.hiresync.model.Candidate;
import com.hiresync.model.User;
import com.hiresync.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EmailService emailService;

    public Optional<Candidate> getCandidateByUserId(Long userId) {
        return candidateRepository.findByUserId(userId);
    }

    public Optional<Candidate> getCandidateById(Long id) {
        return candidateRepository.findById(id);
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    @Transactional
    public Candidate registerCandidate(User user, String phone) {
        Candidate candidate = new Candidate(user, phone);
        Candidate saved = candidateRepository.save(candidate);

        emailService.sendEmail(
                user.getEmail(),
                "Welcome to HireSync!",
                "Dear " + user.getName() + ",\n\nYour candidate registration on HireSync is successful. " +
                "You can now login, set up your profile, upload your resume, and apply for jobs.\n\nRegards,\nHireSync Team"
        );
        return saved;
    }

    @Transactional
    public Candidate updateProfile(Long candidateId, String phone, String education, String skills, String experience, String location) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));

        candidate.setPhone(phone);
        candidate.setEducation(education);
        candidate.setSkills(skills);
        candidate.setExperience(experience);
        candidate.setLocation(location);

        return candidateRepository.save(candidate);
    }

    @Transactional
    public void updateResumePath(Long candidateId, String resumePath) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));
        candidate.setResumePath(resumePath);
        candidateRepository.save(candidate);
    }
}
