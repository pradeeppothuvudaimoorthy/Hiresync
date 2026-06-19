package com.hiresync.controller;

import com.hiresync.model.*;
import com.hiresync.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired
    private UserService userService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobMatchingService jobMatchingService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private NotificationService notificationService;

    private Candidate getLoggedInCandidate(Authentication authentication) {
        if (authentication == null) return null;
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return null;
        return candidateService.getCandidateByUserId(user.getId()).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) {
            return "redirect:/login";
        }

        List<Application> applications = applicationService.getApplicationsByCandidate(candidate.getId());
        List<Interview> interviews = interviewService.getInterviewsForCandidate(candidate.getId());
        List<Notification> notifications = notificationService.getNotificationsForUser(candidate.getUser().getId());
        long unreadNotifications = notificationService.getUnreadCount(candidate.getUser().getId());

        model.addAttribute("candidate", candidate);
        model.addAttribute("appliedCount", applications.size());
        model.addAttribute("interviewCount", interviews.stream().filter(i -> "Scheduled".equalsIgnoreCase(i.getStatus())).count());
        model.addAttribute("applications", applications);
        model.addAttribute("interviews", interviews);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", unreadNotifications);

        return "candidate/candidate-dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        model.addAttribute("candidate", candidate);
        return "candidate/candidate-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            Authentication authentication,
            @RequestParam("phone") String phone,
            @RequestParam("education") String education,
            @RequestParam("skills") String skills,
            @RequestParam("experience") String experience,
            @RequestParam("location") String location,
            RedirectAttributes redirectAttributes) {

        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        if (phone.trim().isEmpty() || skills.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Phone and skills are required.");
            return "redirect:/candidate/profile";
        }

        try {
            candidateService.updateProfile(candidate.getId(), phone, education, skills, experience, location);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/candidate/profile";
    }



    @GetMapping("/applied")
    public String appliedJobs(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        List<Application> applications = applicationService.getApplicationsByCandidate(candidate.getId());
        model.addAttribute("applications", applications);

        // Summary counts for the status cards
        model.addAttribute("shortlistedCount", applications.stream().filter(a -> "Shortlisted".equalsIgnoreCase(a.getStatus())).count());
        model.addAttribute("interviewScheduledCount", applications.stream().filter(a -> "Interview Scheduled".equalsIgnoreCase(a.getStatus())).count());
        model.addAttribute("selectedCount", applications.stream().filter(a -> "Selected".equalsIgnoreCase(a.getStatus())).count());

        return "candidate/applied-jobs";
    }

    @GetMapping("/interviews")
    public String interviewSchedule(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        if (candidate == null) return "redirect:/login";

        List<Interview> interviews = interviewService.getInterviewsForCandidate(candidate.getId());
        model.addAttribute("interviews", interviews);
        return "candidate/interview-schedule";
    }
}
