package com.hiresync.controller;

import com.hiresync.model.*;
import com.hiresync.service.*;
import com.hiresync.repository.JobRepository;
import com.hiresync.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CandidateJobController {

    @Autowired
    private UserService userService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobMatchingService jobMatchingService;

    private Candidate getLoggedInCandidate(Authentication authentication) {
        if (authentication == null) return null;
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return null;
        return candidateService.getCandidateByUserId(user.getId()).orElse(null);
    }

    public static class JobWithScore {
        private final Job job;
        private final int matchScore;
        private final String matchStatus;
        private final boolean alreadyApplied;

        public JobWithScore(Job job, int matchScore, String matchStatus, boolean alreadyApplied) {
            this.job = job;
            this.matchScore = matchScore;
            this.matchStatus = matchStatus;
            this.alreadyApplied = alreadyApplied;
        }

        public Job getJob() { return job; }
        public int getMatchScore() { return matchScore; }
        public String getMatchStatus() { return matchStatus; }
        public boolean isAlreadyApplied() { return alreadyApplied; }
    }

    @GetMapping("/candidate/browse")
    public String browseJobs(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        List<Job> activeJobs = jobRepository.findByStatus(JobStatus.ACTIVE);
        List<JobWithScore> jobsWithScores = new ArrayList<>();

        for (Job job : activeJobs) {
            int score = jobMatchingService.calculateMatchScore(candidate, job);
            String status = getMatchStatusName(score);
            boolean applied = applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId());
            jobsWithScores.add(new JobWithScore(job, score, status, applied));
        }

        // Get Top 5 recommended jobs
        List<JobMatchingService.RecommendedJob> recommended = jobMatchingService.getRecommendations(candidate);
        List<JobMatchingService.RecommendedJob> top5Recommended = recommended.stream().limit(5).collect(Collectors.toList());

        model.addAttribute("candidate", candidate);
        model.addAttribute("jobs", jobsWithScores);
        model.addAttribute("recommendedJobs", top5Recommended);
        model.addAttribute("filter", new JobFilterDTO());

        return "candidate/browse-jobs";
    }

    @GetMapping("/candidate/browse/search")
    public String searchJobs(
            Authentication authentication,
            @ModelAttribute JobFilterDTO filter,
            Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        // Convert filter strings/options into parameter objects
        String keyword = (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) ? filter.getKeyword().trim() : null;
        ExperienceLevel expLvl = (filter.getExperienceLevel() != null && !filter.getExperienceLevel().trim().isEmpty() && !"Any".equalsIgnoreCase(filter.getExperienceLevel())) ? ExperienceLevel.fromString(filter.getExperienceLevel()) : null;
        EmploymentType empType = (filter.getEmploymentType() != null && !filter.getEmploymentType().trim().isEmpty() && !"Any".equalsIgnoreCase(filter.getEmploymentType())) ? EmploymentType.fromString(filter.getEmploymentType()) : null;
        WorkplaceType workType = (filter.getWorkplaceType() != null && !filter.getWorkplaceType().trim().isEmpty() && !"Any".equalsIgnoreCase(filter.getWorkplaceType())) ? WorkplaceType.fromString(filter.getWorkplaceType()) : null;
        String location = (filter.getLocation() != null && !filter.getLocation().trim().isEmpty()) ? filter.getLocation().trim() : null;
        String companyName = (filter.getCompanyName() != null && !filter.getCompanyName().trim().isEmpty()) ? filter.getCompanyName().trim() : null;
        String industry = (filter.getIndustry() != null && !filter.getIndustry().trim().isEmpty()) ? filter.getIndustry().trim() : null;
        Boolean easyApply = (filter.getEasyApply() != null && filter.getEasyApply()) ? true : null;
        
        Integer appLimit = null;
        if (filter.getApplicantCountLimit() != null && filter.getApplicantCountLimit() > 0) {
            appLimit = filter.getApplicantCountLimit();
        }
        
        Double minSalary = filter.getMinimumSalary() != null && filter.getMinimumSalary() > 0 ? filter.getMinimumSalary() : null;

        LocalDate startDate = null;
        if (filter.getDatePosted() != null && !filter.getDatePosted().trim().isEmpty() && !"Any Time".equalsIgnoreCase(filter.getDatePosted())) {
            DatePostedFilter dpf = DatePostedFilter.fromString(filter.getDatePosted());
            if (dpf == DatePostedFilter.LAST_24_HOURS) {
                startDate = LocalDate.now().minusDays(1);
            } else if (dpf == DatePostedFilter.LAST_7_DAYS) {
                startDate = LocalDate.now().minusDays(7);
            } else if (dpf == DatePostedFilter.LAST_30_DAYS) {
                startDate = LocalDate.now().minusDays(30);
            }
        }

        List<Job> filteredJobs = jobRepository.searchJobsWithFilters(
                JobStatus.ACTIVE, keyword, expLvl, empType, workType, location,
                companyName, industry, easyApply, appLimit, minSalary, startDate
        );

        List<JobWithScore> jobsWithScores = new ArrayList<>();
        for (Job job : filteredJobs) {
            int score = jobMatchingService.calculateMatchScore(candidate, job);
            String status = getMatchStatusName(score);
            boolean applied = applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId());
            jobsWithScores.add(new JobWithScore(job, score, status, applied));
        }

        // Get Top 5 recommended jobs
        List<JobMatchingService.RecommendedJob> recommended = jobMatchingService.getRecommendations(candidate);
        List<JobMatchingService.RecommendedJob> top5Recommended = recommended.stream().limit(5).collect(Collectors.toList());

        model.addAttribute("candidate", candidate);
        model.addAttribute("jobs", jobsWithScores);
        model.addAttribute("recommendedJobs", top5Recommended);
        model.addAttribute("filter", filter);

        return "candidate/browse-jobs";
    }

    @GetMapping("/candidate/jobs/{id}")
    public String viewJobDetails(
            Authentication authentication,
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            redirectAttributes.addFlashAttribute("error", "Job posting not found.");
            return "redirect:/candidate/browse";
        }

        int score = jobMatchingService.calculateMatchScore(candidate, job);
        String status = getMatchStatusName(score);
        boolean applied = applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId());

        model.addAttribute("candidate", candidate);
        model.addAttribute("job", job);
        model.addAttribute("matchScore", score);
        model.addAttribute("matchStatus", status);
        model.addAttribute("alreadyApplied", applied);

        return "candidate/job-details";
    }

    @PostMapping("/candidate/jobs/apply/{jobId}")
    public String applyForJob(
            Authentication authentication,
            @PathVariable("jobId") Long jobId,
            RedirectAttributes redirectAttributes) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        try {
            applicationService.applyForJob(candidate, jobId);
            redirectAttributes.addFlashAttribute("success", "Application submitted successfully!");
            return "redirect:/candidate/applied";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/candidate/jobs/" + jobId;
        }
    }

    @GetMapping("/candidate/recommended")
    public String viewAllRecommended(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        List<JobMatchingService.RecommendedJob> recommended = jobMatchingService.getRecommendations(candidate);
        model.addAttribute("candidate", candidate);
        model.addAttribute("recommendations", recommended);

        return "candidate/recommended-jobs";
    }

    private String getMatchStatusName(int score) {
        if (score >= 80) return "High Match";
        if (score >= 50) return "Medium Match";
        return "Low Match";
    }
}
