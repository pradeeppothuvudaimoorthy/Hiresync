package com.hiresync.controller;

import com.hiresync.model.Job;
import com.hiresync.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/{id}")
    public String viewJobDetails(@PathVariable("id") Long id, Model model) {
        Job job = jobService.getJobById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + id));
        model.addAttribute("job", job);
        return "job-details"; // We can create a simple job-details common view
    }
}
