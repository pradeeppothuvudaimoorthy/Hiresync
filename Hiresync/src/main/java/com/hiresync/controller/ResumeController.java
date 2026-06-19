package com.hiresync.controller;

import com.hiresync.entity.Resume;
import com.hiresync.model.Application;
import com.hiresync.model.Candidate;
import com.hiresync.model.Recruiter;
import com.hiresync.model.User;
import com.hiresync.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@Controller
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private RecruiterService recruiterService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/candidate/resume/upload")
    public String showUploadPage(Authentication authentication, Model model) {
        if (authentication == null) return "redirect:/login";
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        
        Candidate candidate = candidateService.getCandidateByUserId(user.getId()).orElse(null);
        model.addAttribute("candidate", candidate);
        return "upload-resume"; // Renders src/main/resources/templates/upload-resume.html
    }

    @PostMapping("/candidate/resume/upload")
    public String uploadResume(
            Authentication authentication,
            @RequestParam("resumeFile") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) return "redirect:/login";
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Candidate candidate = candidateService.getCandidateByUserId(user.getId()).orElse(null);
        if (candidate == null) {
            redirectAttributes.addFlashAttribute("error", "Only candidates can upload resumes.");
            return "redirect:/";
        }

        try {
            if (file == null || file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a resume file.");
                return "redirect:/candidate/resume/upload";
            }

            Resume resume = resumeService.uploadResume(file, candidate);
            candidateService.updateResumePath(candidate.getId(), resume.getStoredFileName());
            redirectAttributes.addFlashAttribute("success", "Resume uploaded successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Could not store the file. Please try again! " + e.getMessage());
        }

        return "redirect:/candidate/resume/upload";
    }

    @GetMapping("/recruiter/resume/download/{applicationId}")
    public ResponseEntity<Resource> downloadResume(
            Authentication authentication,
            @PathVariable("applicationId") Long applicationId) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        Recruiter recruiter = recruiterService.getRecruiterByUserId(user.getId()).orElse(null);
        if (recruiter == null) {
            return ResponseEntity.status(403).build(); // Only recruiters can download resumes from this endpoint
        }

        // Fetch application
        Application application = applicationService.getApplicationById(applicationId).orElse(null);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }

        // Security check: Recruiter can access resume only if candidate applied to recruiter's job
        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            return ResponseEntity.status(403).build();
        }

        String filename = application.getCandidate().getResumePath();
        if (filename == null || filename.trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path file = resumeService.loadResume(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = "application/octet-stream";
                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (filename.endsWith(".docx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                } else if (filename.endsWith(".doc")) {
                    contentType = "application/msword";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
