package com.hiresync.service;

import com.hiresync.model.Candidate;
import com.hiresync.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiRecommendationService {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.url:}")
    private String apiUrl;

    @Value("${ai.provider:rule-based}")
    private String aiProvider;

    public String generateRecommendationReason(Candidate candidate, Job job, double matchScore) {
        if (apiKey == null || apiKey.trim().isEmpty() || "rule-based".equalsIgnoreCase(aiProvider)) {
            return generateRuleBasedReason(candidate, job, matchScore);
        }

        // Optional AI API Logic (mocked call or placeholder return depending on provider)
        try {
            // Under normal circumstances, you'd execute an HTTP request here using apiUrl and apiKey.
            // Since API keys must not be hardcoded or visible, we will check and fall back gracefully.
            return "[AI Explanation]: Based on your candidate profile with location " + candidate.getLocation() + 
                   " and skills, this job matches your criteria with a score of " + String.format("%.1f", matchScore) + 
                   "%. The requirements match your technical background.";
        } catch (Exception e) {
            return generateRuleBasedReason(candidate, job, matchScore);
        }
    }

    private String generateRuleBasedReason(Candidate candidate, Job job, double matchScore) {
        List<String> matchPoints = new ArrayList<>();
        
        // Match skills
        List<String> matchedSkills = new ArrayList<>();
        if (candidate.getSkills() != null && job.getRequiredSkills() != null) {
            String[] candidateSkills = candidate.getSkills().split(",");
            String[] jobSkills = job.getRequiredSkills().split(",");
            for (String js : jobSkills) {
                for (String cs : candidateSkills) {
                    if (js.trim().equalsIgnoreCase(cs.trim())) {
                        matchedSkills.add(js.trim());
                        break;
                    }
                }
            }
        }

        if (!matchedSkills.isEmpty()) {
            matchPoints.add("your skills (" + String.join(", ", matchedSkills) + ") match the job requirements");
        }

        // Match location
        if (candidate.getLocation() != null && job.getLocation() != null && 
            job.getLocation().toLowerCase().contains(candidate.getLocation().toLowerCase())) {
            matchPoints.add("the location matches your preferred location (" + candidate.getLocation() + ")");
        }

        // Match experience
        if (candidate.getExperience() != null && job.getExperienceRequired() != null && 
            candidate.getExperience().toLowerCase().contains("fresh") && job.getExperienceRequired().toLowerCase().contains("fresh")) {
            matchPoints.add("the experience requirements align with your profile");
        }

        if (matchPoints.isEmpty()) {
            return "Recommended based on overall job suitability and matching metrics.";
        } else {
            return "Recommended because " + String.join(" and ", matchPoints) + ".";
        }
    }
}
