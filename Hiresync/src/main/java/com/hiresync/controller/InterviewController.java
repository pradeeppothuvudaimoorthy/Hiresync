package com.hiresync.controller;

import com.hiresync.model.Application;
import com.hiresync.model.Interview;
import com.hiresync.model.Recruiter;
import com.hiresync.model.User;
import com.hiresync.service.ApplicationService;
import com.hiresync.service.InterviewService;
import com.hiresync.service.RecruiterService;
import com.hiresync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/recruiter/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecruiterService recruiterService;

    private Recruiter getLoggedInRecruiter(Authentication authentication) {
        if (authentication == null) return null;
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return null;
        return recruiterService.getRecruiterByUserId(user.getId()).orElse(null);
    }

    @GetMapping
    public String viewSchedules(Authentication authentication, Model model) {
        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        List<Interview> interviews = interviewService.getInterviewsForRecruiter(recruiter.getId());
        model.addAttribute("interviews", interviews);
        return "recruiter/interview-schedule";
    }

    @GetMapping("/schedule/{applicationId}")
    public String showScheduleForm(
            Authentication authentication,
            @PathVariable("applicationId") Long applicationId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            Application application = applicationService.getApplicationById(applicationId)
                    .orElseThrow(() -> new IllegalArgumentException("Application not found."));

            // Safety check
            if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
                throw new SecurityException("Unauthorized access.");
            }

            model.addAttribute("application", application);
            // Pre-fill existing interview if any
            Interview interview = interviewService.getInterviewByApplicationId(applicationId).orElse(new Interview());
            model.addAttribute("interview", interview);
            
            return "recruiter/schedule-interview";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recruiter/shortlisted";
        }
    }

    @PostMapping("/schedule/{applicationId}")
    public String scheduleInterview(
            Authentication authentication,
            @PathVariable("applicationId") Long applicationId,
            @RequestParam("interviewDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("interviewTime") String timeStr,
            @RequestParam("mode") String mode,
            @RequestParam(value = "meetingLink", required = false) String meetingLink,
            @RequestParam(value = "location", required = false) String location,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            LocalTime time = LocalTime.parse(timeStr);
            interviewService.scheduleInterview(applicationId, date, time, mode, meetingLink, location, recruiter);
            redirectAttributes.addFlashAttribute("success", "Interview scheduled successfully!");
            return "redirect:/recruiter/interviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recruiter/interviews/schedule/" + applicationId;
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelInterview(
            Authentication authentication,
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter = getLoggedInRecruiter(authentication);
        if (recruiter == null) return "redirect:/login";

        try {
            interviewService.cancelInterview(id, recruiter);
            redirectAttributes.addFlashAttribute("success", "Interview cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/recruiter/interviews";
    }
}
