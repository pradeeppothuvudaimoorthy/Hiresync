package com.hiresync.service;

import com.hiresync.model.Job;
import com.hiresync.model.Recruiter;
import com.hiresync.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getActiveJobs() {
        return jobRepository.findByStatus(com.hiresync.model.JobStatus.ACTIVE);
    }

    public List<Job> getJobsByRecruiter(Long recruiterId) {
        return jobRepository.findByRecruiterId(recruiterId);
    }

    @Transactional
    public Job createJob(Job job, Recruiter recruiter) {
        job.setRecruiter(recruiter);
        job.setCompanyName(recruiter.getCompanyName());
        job.setStatus("ACTIVE");
        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJob(Long jobId, Job details, Recruiter recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        // Security check: Only the owner recruiter can modify
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You do not have permission to modify this job.");
        }

        job.setTitle(details.getTitle());
        job.setDescription(details.getDescription());
        job.setRequiredSkills(details.getRequiredSkills());
        job.setExperienceRequired(details.getExperienceRequired());
        job.setLocation(details.getLocation());
        job.setSalaryRange(details.getSalaryRange());
        job.setJobType(details.getJobType());
        job.setOpenings(details.getOpenings());
        job.setLastDateToApply(details.getLastDateToApply());
        if (details.getStatus() != null) {
            job.setStatus(details.getStatus());
        }

        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long jobId, Recruiter recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You do not have permission to delete this job.");
        }

        jobRepository.delete(job);
    }

    @Transactional
    public void closeJob(Long jobId, Recruiter recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new SecurityException("You do not have permission to close this job.");
        }

        job.setStatus("CLOSED");
        jobRepository.save(job);
    }

    public List<Job> searchJobs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getActiveJobs();
        }
        return jobRepository.searchJobs(query.trim());
    }
}
