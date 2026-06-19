package com.hiresync.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ScreeningService {

    public static class MatchResult {
        private final double score;
        private final String status;

        public MatchResult(double score, String status) {
            this.score = score;
            this.status = status;
        }

        public double getScore() { return score; }
        public String getStatus() { return status; }
    }

    public MatchResult calculateMatchScore(String candidateSkills, String jobSkills) {
        if (jobSkills == null || jobSkills.trim().isEmpty()) {
            return new MatchResult(100.0, "High Match");
        }
        if (candidateSkills == null || candidateSkills.trim().isEmpty()) {
            return new MatchResult(0.0, "Low Match");
        }

        // Clean and tokenize job skills
        Set<String> jobSkillsSet = parseSkills(jobSkills);
        // Clean and tokenize candidate skills
        Set<String> candidateSkillsSet = parseSkills(candidateSkills);

        if (jobSkillsSet.isEmpty()) {
            return new MatchResult(100.0, "High Match");
        }

        long matchCount = jobSkillsSet.stream()
                .filter(candidateSkillsSet::contains)
                .count();

        double score = ((double) matchCount / jobSkillsSet.size()) * 100.0;
        // Round to 1 decimal place
        score = Math.round(score * 10.0) / 10.0;

        String status;
        if (score >= 80.0) {
            status = "High Match";
        } else if (score >= 50.0) {
            status = "Medium Match";
        } else {
            status = "Low Match";
        }

        return new MatchResult(score, status);
    }

    private Set<String> parseSkills(String skillsString) {
        Set<String> skillsSet = new HashSet<>();
        if (skillsString == null) {
            return skillsSet;
        }
        String[] tokens = skillsString.split(",");
        for (String token : tokens) {
            String cleanSkill = token.trim().toLowerCase().replaceAll("\\s+", " ");
            if (!cleanSkill.isEmpty()) {
                skillsSet.add(cleanSkill);
            }
        }
        return skillsSet;
    }
}
