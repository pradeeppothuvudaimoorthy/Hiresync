package com.hiresync.controller;

import com.hiresync.model.User;
import com.hiresync.model.UserRole;
import com.hiresync.service.CandidateService;
import com.hiresync.service.RecruiterService;
import com.hiresync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private RecruiterService recruiterService;

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (roles.contains("ROLE_RECRUITER")) {
                return "redirect:/recruiter/dashboard";
            } else if (roles.contains("ROLE_CANDIDATE")) {
                return "redirect:/candidate/dashboard";
            }
        }
        return "home";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register/candidate")
    public String registerCandidate(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes) {

        if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty() || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "All fields are required.");
            return "redirect:/register?tab=candidate";
        }

        try {
            User user = userService.registerUser(name, email, password, UserRole.CANDIDATE);
            candidateService.registerCandidate(user, phone);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register?tab=candidate";
        }
    }

    @PostMapping("/register/recruiter")
    public String registerRecruiter(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("companyName") String companyName,
            @RequestParam("companyWebsite") String companyWebsite,
            @RequestParam("companyLocation") String companyLocation,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes) {

        if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty() || 
            companyName.trim().isEmpty() || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Name, Email, Password, Company Name, and Phone are required.");
            return "redirect:/register?tab=recruiter";
        }

        try {
            User user = userService.registerUser(name, email, password, UserRole.RECRUITER);
            recruiterService.registerRecruiter(user, companyName, companyWebsite, companyLocation, phone);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Your account is pending administrator approval.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register?tab=recruiter";
        }
    }
}
