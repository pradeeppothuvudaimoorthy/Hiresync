package com.hiresync.controller;

import com.hiresync.model.Candidate;
import com.hiresync.model.Recruiter;
import com.hiresync.model.User;
import com.hiresync.service.ApplicationService;
import com.hiresync.service.CandidateService;
import com.hiresync.service.RecruiterService;
import com.hiresync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private RecruiterService recruiterService;

    @PostMapping("/candidate/apply/{jobId}")
    public String applyForJob(
            Authentication authentication,
            @PathVariable("jobId") Long jobId,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) return "redirect:/login";

        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Candidate candidate = candidateService.getCandidateByUserId(user.getId()).orElse(null);
        if (candidate == null) {
            redirectAttributes.addFlashAttribute("error", "Only candidates can apply for jobs.");
            return "redirect:/";
        }

        try {
            applicationService.applyForJob(candidate, jobId);
            redirectAttributes.addFlashAttribute("success", "Your application has been submitted successfully!");
            return "redirect:/candidate/applied";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/candidate/browse";
        }
    }

    @PostMapping("/recruiter/applications/status/{id}")
    public String updateApplicationStatus(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) return "redirect:/login";

        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Recruiter recruiter = recruiterService.getRecruiterByUserId(user.getId()).orElse(null);
        if (recruiter == null) return "redirect:/";

        try {
            applicationService.updateApplicationStatus(id, status, recruiter);
            redirectAttributes.addFlashAttribute("success", "Application status updated to: " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/recruiter/applications";
    }
}
