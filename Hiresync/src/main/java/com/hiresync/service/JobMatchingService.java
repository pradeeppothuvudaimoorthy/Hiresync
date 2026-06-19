package com.hiresync.service;

import com.hiresync.model.*;
import com.hiresync.repository.ApplicationRepository;
import com.hiresync.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobMatchingService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AiRecommendationService aiRecommendationService;

    public static class RecommendedJob {
        private final Job job;
        private final double matchScore;
        private final String matchStatus;
        private final String recommendationReason;

        public RecommendedJob(Job job, double matchScore, String matchStatus, String recommendationReason) {
            this.job = job;
            this.matchScore = matchScore;
            this.matchStatus = matchStatus;
            this.recommendationReason = recommendationReason;
        }

        public Job getJob() { return job; }
        public double getMatchScore() { return matchScore; }
        public String getMatchStatus() { return matchStatus; }
        public String getRecommendationReason() { return recommendationReason; }
    }

    public int calculateMatchScore(Candidate candidate, Job job) {
        // 1. Skill match = 70%
        double skillScore = 0.0;
        if (job.getRequiredSkills() == null || job.getRequiredSkills().trim().isEmpty()) {
            skillScore = 70.0;
        } else if (candidate.getSkills() != null && !candidate.getSkills().trim().isEmpty()) {
            String[] jobSkills = job.getRequiredSkills().split(",");
            String[] candidateSkills = candidate.getSkills().split(",");

            int matchedCount = 0;
            int totalJobSkills = jobSkills.length;

            for (String js : jobSkills) {
                String cleanJs = js.trim().toLowerCase();
                for (String cs : candidateSkills) {
                    String cleanCs = cs.trim().toLowerCase();
                    if (cleanJs.equals(cleanCs)) {
                        matchedCount++;
                        break;
                    }
                }
            }
            skillScore = ((double) matchedCount / totalJobSkills) * 70.0;
        }

        // 2. Location match = 10%
        int locationScore = 0;
        if (candidate.getLocation() != null && !candidate.getLocation().trim().isEmpty() &&
            job.getJobLocation() != null && !job.getJobLocation().trim().isEmpty()) {
            
            String cLoc = candidate.getLocation().toLowerCase().trim();
            if (job.getJobLocation().toLowerCase().contains(cLoc) ||
                (job.getCity() != null && job.getCity().toLowerCase().contains(cLoc)) ||
                (job.getState() != null && job.getState().toLowerCase().contains(cLoc)) ||
                (job.getCountry() != null && job.getCountry().toLowerCase().contains(cLoc)) ||
                ("remote".equalsIgnoreCase(cLoc) && job.getWorkplaceType() == WorkplaceType.REMOTE)) {
                locationScore = 10;
            }
        }

        // 3. Experience match = 10%
        int experienceScore = 0;
        if (candidate.getExperience() != null && !candidate.getExperience().trim().isEmpty() &&
            job.getExperienceRequired() != null && !job.getExperienceRequired().trim().isEmpty()) {

            String cExp = candidate.getExperience().toLowerCase();
            String jExp = job.getExperienceRequired().toLowerCase();

            if ((cExp.contains("fresh") || cExp.contains("0")) && 
                (jExp.contains("fresh") || jExp.contains("0") || job.getExperienceLevel() == ExperienceLevel.ENTRY_LEVEL)) {
                experienceScore = 10;
            } else {
                int cYears = extractYears(cExp);
                int jYears = extractYears(jExp);
                if (cYears >= jYears) {
                    experienceScore = 10;
                }
            }
        } else if (job.getExperienceLevel() == ExperienceLevel.ENTRY_LEVEL) {
            experienceScore = 10;
        }

        // 4. Education match = 10%
        int educationScore = 0;
        if (job.getEducationRequired() == null || job.getEducationRequired().trim().isEmpty() || "any".equalsIgnoreCase(job.getEducationRequired())) {
            educationScore = 10;
        } else if (candidate.getEducation() != null && !candidate.getEducation().trim().isEmpty()) {
            String cEd = candidate.getEducation().toLowerCase();
            String jEd = job.getEducationRequired().toLowerCase();
            if (cEd.contains(jEd) || jEd.contains(cEd) ||
                ((cEd.contains("bachelor") || cEd.contains("btech") || cEd.contains("b.tech") || cEd.contains("be") || cEd.contains("degree")) &&
                 (jEd.contains("bachelor") || jEd.contains("btech") || jEd.contains("b.tech") || jEd.contains("be")))) {
                educationScore = 10;
            }
        }

        int totalScore = (int) Math.round(skillScore) + locationScore + experienceScore + educationScore;
        return Math.min(100, Math.max(0, totalScore));
    }

    private int extractYears(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<RecommendedJob> getRecommendations(Candidate candidate) {
        List<RecommendedJob> recommendations = new ArrayList<>();
        List<Job> activeJobs = jobRepository.findByStatus(JobStatus.ACTIVE);

        for (Job job : activeJobs) {
            // Check if the candidate has already applied to this job
            boolean alreadyApplied = applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId());
            if (alreadyApplied) {
                continue;
            }

            int matchScore = calculateMatchScore(candidate, job);
            
            MatchStatus matchStatus;
            if (matchScore >= 80) {
                matchStatus = MatchStatus.HIGH_MATCH;
            } else if (matchScore >= 50) {
                matchStatus = MatchStatus.MEDIUM_MATCH;
            } else {
                matchStatus = MatchStatus.LOW_MATCH;
            }

            String reason = aiRecommendationService.generateRecommendationReason(candidate, job, matchScore);
            recommendations.add(new RecommendedJob(job, matchScore, matchStatus.getDisplayName(), reason));
        }

        // Sort by match score descending
        recommendations.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        return recommendations;
    }
}
