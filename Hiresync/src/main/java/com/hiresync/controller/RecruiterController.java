package com.hiresync.controller;

import com.hiresync.model.*;
import com.hiresync.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecruiterService recruiterService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private NotificationService notificationService;

    private Recruiter getLoggedInRecruiter(Authentication authentication) {
        if (authentication == null) return null;
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return null;
        return recruiterService.getRecruiterByUserId(user.getId()).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) {
            return "redirect:/login";
        }

        // Pass status check to model
        model.addAttribute("recruiterStatus", recruiter.getStatus());
        model.addAttribute("recruiter", recruiter);

        if (!"APPROVED".equalsIgnoreCase(recruiter.getStatus())) {
            return "recruiter/recruiter-dashboard";
        }

        // Stats calculation
        List<Job> jobs = jobService.getJobsByRecruiter(recruiter.getId());
        List<Application> applications = applicationService.getApplicationsByRecruiter(recruiter.getId());
        List<Interview> interviews = interviewService.getInterviewsForRecruiter(recruiter.getId());

        long totalJobs = jobs.size();
        long totalApplications = applications.size();
        long shortlistedCount = applications.stream().filter(a -> "Shortlisted".equalsIgnoreCase(a.getStatus())).count();
        long rejectedCount = applications.stream().filter(a -> "Rejected".equalsIgnoreCase(a.getStatus())).count();
        long selectedCount = applications.stream().filter(a -> "Selected".equalsIgnoreCase(a.getStatus())).count();

        // Upcoming interviews (interviews on or after today and not cancelled)
        List<Interview> upcomingInterviews = interviews.stream()
                .filter(i -> !i.getInterviewDate().isBefore(LocalDate.now()) && !"Cancelled".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList());

        // Recent 5 applications
        List<Application> recentApplications = applications.stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .limit(5)
                .collect(Collectors.toList());

        // Unread notification count
        long unreadNotifications = notificationService.getUnreadCount(recruiter.getUser().getId());

        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("shortlistedCount", shortlistedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("selectedCount", selectedCount);
        model.addAttribute("upcomingInterviews", upcomingInterviews);
        model.addAttribute("recentApplications", recentApplications);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("jobs", jobs);

        return "recruiter/recruiter-dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        model.addAttribute("recruiter", recruiter);
        return "recruiter/recruiter-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            Authentication authentication,
            @RequestParam("companyName") String companyName,
            @RequestParam("companyWebsite") String companyWebsite,
            @RequestParam("companyLocation") String companyLocation,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        if (companyName.trim().isEmpty() || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Company name and Phone are required.");
            return "redirect:/recruiter/profile";
        }

        try {
            recruiterService.updateProfile(recruiter.getId(), companyName, companyWebsite, companyLocation, phone);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/recruiter/profile";
    }

    @GetMapping("/jobs")
    public String manageJobs(Authentication authentication, Model model) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        if (!"APPROVED".equalsIgnoreCase(recruiter.getStatus())) {
            return "redirect:/recruiter/dashboard";
        }

        List<Job> jobs = jobService.getJobsByRecruiter(recruiter.getId());
        model.addAttribute("jobs", jobs);
        return "recruiter/manage-jobs";
    }

    @GetMapping("/jobs/post")
    public String showPostJobForm(Authentication authentication, Model model) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        if (!"APPROVED".equalsIgnoreCase(recruiter.getStatus())) {
            return "redirect:/recruiter/dashboard";
        }

        model.addAttribute("job", new Job());
        return "recruiter/post-job";
    }

    @PostMapping("/jobs/post")
    public String postJob(
            Authentication authentication,
            @ModelAttribute Job job,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        if (!"APPROVED".equalsIgnoreCase(recruiter.getStatus())) {
            return "redirect:/recruiter/dashboard";
        }

        if (job.getTitle().trim().isEmpty() || job.getRequiredSkills().trim().isEmpty() || 
            job.getExperienceRequired().trim().isEmpty() || job.getLocation().trim().isEmpty() || 
            job.getLastDateToApply() == null || job.getOpenings() == null) {
            redirectAttributes.addFlashAttribute("error", "All fields are required.");
            return "redirect:/recruiter/jobs/post";
        }

        try {
            jobService.createJob(job, recruiter);
            redirectAttributes.addFlashAttribute("success", "Job posted successfully.");
            return "redirect:/recruiter/jobs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recruiter/jobs/post";
        }
    }

    @GetMapping("/jobs/edit/{id}")
    public String showEditJobForm(
            Authentication authentication,
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            Job job = jobService.getJobById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found."));
            if (!job.getRecruiter().getId().equals(recruiter.getId())) {
                throw new SecurityException("Unauthorized access.");
            }
            model.addAttribute("job", job);
            return "recruiter/edit-job";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recruiter/jobs";
        }
    }

    @PostMapping("/jobs/edit/{id}")
    public String editJob(
            Authentication authentication,
            @PathVariable("id") Long id,
            @ModelAttribute Job jobDetails,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            jobService.updateJob(id, jobDetails, recruiter);
            redirectAttributes.addFlashAttribute("success", "Job updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/recruiter/jobs";
    }

    @PostMapping("/jobs/close/{id}")
    public String closeJob(Authentication authentication, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            jobService.closeJob(id, recruiter);
            redirectAttributes.addFlashAttribute("success", "Job closed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recruiter/jobs";
    }

    @PostMapping("/jobs/delete/{id}")
    public String deleteJob(Authentication authentication, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            jobService.deleteJob(id, recruiter);
            redirectAttributes.addFlashAttribute("success", "Job deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recruiter/jobs";
    }

    @GetMapping("/applications")
    public String viewApplicants(Authentication authentication, Model model) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        if (!"APPROVED".equalsIgnoreCase(recruiter.getStatus())) {
            return "redirect:/recruiter/dashboard";
        }

        List<Application> applications = applicationService.getApplicationsByRecruiter(recruiter.getId());
        
        // Sort candidates by match score descending (AI Resume Screening Ranking)
        applications.sort((a, b) -> Double.compare(b.getMatchScore() != null ? b.getMatchScore() : 0.0, 
                                                   a.getMatchScore() != null ? a.getMatchScore() : 0.0));

        model.addAttribute("applications", applications);
        return "recruiter/view-applicants";
    }

    @GetMapping("/shortlisted")
    public String viewShortlisted(Authentication authentication, Model model) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        if (!"APPROVED".equalsIgnoreCase(recruiter.getStatus())) {
            return "redirect:/recruiter/dashboard";
        }

        List<Application> applications = applicationService.getApplicationsByRecruiter(recruiter.getId()).stream()
                .filter(a -> "Shortlisted".equalsIgnoreCase(a.getStatus()) || "Interview Scheduled".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("applications", applications);
        return "recruiter/shortlisted-candidates";
    }

    @PostMapping("/dev/approve")
    public String devApprove(Authentication authentication, RedirectAttributes redirectAttributes) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            recruiterService.updateStatus(recruiter.getId(), "APPROVED");
            redirectAttributes.addFlashAttribute("success", "[Dev Mode] Recruiter account approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve: " + e.getMessage());
        }
        return "redirect:/recruiter/dashboard";
    }
}
