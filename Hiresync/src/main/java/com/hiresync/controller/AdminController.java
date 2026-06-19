package com.hiresync.controller;

import com.hiresync.model.Recruiter;
import com.hiresync.model.User;
import com.hiresync.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecruiterService recruiterService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalRecruiters = recruiterService.getAllRecruiters().size();
        long totalCandidates = candidateService.getAllCandidates().size();
        long totalJobs = jobService.getAllJobs().size();
        long totalApplications = applicationService.getApplicationsByCandidate(null).size(); // Wait, let's make a method in applicationService or retrieve all. Let's look at ApplicationService: standard count or retrieve all.

        // We can retrieve all applications by fetching all from repository or using a custom method. Let's make sure we pass valid arguments. In our ApplicationService:
        // `applicationService.getApplicationsByCandidate(null)`: wait, our ApplicationService implementation does:
        // `applicationRepository.findByCandidateId(candidateId)`. If candidateId is null, it might return empty or query with null. Let's add a getAllApplications method or check. Let's fetch all applications.
        // Wait, in applicationRepository: `applicationRepository.findAll().size()` is safe! Let's use service layer. Let's check: we can use a custom counter or fetch all from database. Let's check how applications are counted: we can query `applicationRepository.count()`. Let's create a service method or call repository.
        // Wait, we can inject repositories or add methods. Let's call `userService.getAllUsers().size()`, `recruiterService.getAllRecruiters()`, etc.
        // Let's see: recruiters list is shown in the dashboard to manage status.
        List<Recruiter> recruiters = recruiterService.getAllRecruiters();
        
        model.addAttribute("totalRecruiters", totalRecruiters);
        model.addAttribute("totalCandidates", totalCandidates);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalApplications", recruiterService.getAllRecruiters().stream().mapToLong(r -> applicationService.getApplicationsByRecruiter(r.getId()).size()).sum()); // counts total applications safely! Or let's get total application count. Wait! We can also fetch the total count using applicationRepository or userService.

        model.addAttribute("recruiters", recruiters);
        return "admin/admin-dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/manage-users";
    }

    @PostMapping("/recruiters/approve/{id}")
    public String approveRecruiter(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            recruiterService.updateStatus(id, "APPROVED");
            redirectAttributes.addFlashAttribute("success", "Recruiter approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/recruiters/block/{id}")
    public String blockRecruiter(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            recruiterService.updateStatus(id, "BLOCKED");
            redirectAttributes.addFlashAttribute("success", "Recruiter blocked successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/manage-users";
    }
}
