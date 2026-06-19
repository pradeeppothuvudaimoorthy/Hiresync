package com.hiresync.controller;

import com.hiresync.model.User;
import com.hiresync.service.NotificationService;
import com.hiresync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private String getDashboardRedirect(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (roles.contains("ROLE_RECRUITER")) {
            return "redirect:/recruiter/dashboard";
        } else if (roles.contains("ROLE_CANDIDATE")) {
            return "redirect:/candidate/dashboard";
        }
        return "redirect:/";
    }

    @PostMapping("/read/{id}")
    public String markAsRead(
            Authentication authentication,
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) return "redirect:/login";

        try {
            notificationService.markAsRead(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return getDashboardRedirect(authentication);
    }

    @PostMapping("/read-all")
    public String markAllAsRead(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) return "redirect:/login";

        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user != null) {
            try {
                notificationService.markAllAsRead(user.getId());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }

        return getDashboardRedirect(authentication);
    }
}
