package com.hiresync.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", referencedColumnName = "id", nullable = false)
    private Recruiter recruiter;

    @Column(name = "title", nullable = false)
    private String jobTitle;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_logo")
    private String companyLogo;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;

    @Column(name = "required_skills", columnDefinition = "TEXT", nullable = false)
    private String requiredSkills; // Comma separated, e.g., "Java, Spring Boot"

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel = ExperienceLevel.ENTRY_LEVEL;

    @Column(name = "experience_required", nullable = false)
    private String experienceRequired; // e.g. "2 years"

    @Column(name = "education_required")
    private String educationRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "workplace_type", nullable = false)
    private WorkplaceType workplaceType = WorkplaceType.ONSITE;

    @Column(name = "location", nullable = false)
    private String jobLocation;

    private String city;
    private String state;
    private String country;
    private String industry;

    @Column(name = "salary_min")
    private Double salaryMin;

    @Column(name = "salary_max")
    private Double salaryMax;

    @Column(name = "salary_range")
    private String salaryRange; // For compatibility, e.g. "$80,000 - $100,000"

    @Column(name = "openings", nullable = false)
    private Integer numberOfOpenings;

    @Column(name = "applicant_count", nullable = false)
    private Integer applicantCount = 0;

    @Column(name = "easy_apply", nullable = false)
    private boolean easyApply = false;

    @Column(name = "last_date_to_apply", nullable = false)
    private LocalDate lastDateToApply;

    @Column(name = "posted_date", nullable = false)
    private LocalDate postedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Job() {}

    @PrePersist
    protected void onCreate() {
        this.postedDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Backwards Compatibility Accessors (Aliased Getters/Setters) ---
    public String getTitle() {
        return this.jobTitle;
    }

    public void setTitle(String title) {
        this.jobTitle = title;
    }

    public String getDescription() {
        return this.jobDescription;
    }

    public void setDescription(String description) {
        this.jobDescription = description;
    }

    public String getLocation() {
        return this.jobLocation;
    }

    public void setLocation(String location) {
        this.jobLocation = location;
    }

    public Integer getOpenings() {
        return this.numberOfOpenings;
    }

    public void setOpenings(Integer openings) {
        this.numberOfOpenings = openings;
    }

    public String getJobType() {
        return this.employmentType != null ? this.employmentType.getDisplayName() : "Full-time";
    }

    public void setJobType(String typeStr) {
        this.employmentType = EmploymentType.fromString(typeStr);
    }

    public String getStatus() {
        return this.status != null ? this.status.name() : "ACTIVE";
    }

    public void setStatus(String statusStr) {
        if (statusStr == null) {
            this.status = JobStatus.ACTIVE;
        } else {
            try {
                this.status = JobStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.status = JobStatus.ACTIVE;
            }
        }
    }

    // --- Standard Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Recruiter getRecruiter() { return recruiter; }
    public void setRecruiter(Recruiter recruiter) { this.recruiter = recruiter; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(String experienceRequired) { this.experienceRequired = experienceRequired; }

    public String getEducationRequired() { return educationRequired; }
    public void setEducationRequired(String educationRequired) { this.educationRequired = educationRequired; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public WorkplaceType getWorkplaceType() { return workplaceType; }
    public void setWorkplaceType(WorkplaceType workplaceType) { this.workplaceType = workplaceType; }

    public String getJobLocation() { return jobLocation; }
    public void setJobLocation(String jobLocation) { this.jobLocation = jobLocation; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public Double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Double salaryMin) { this.salaryMin = salaryMin; }

    public Double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Double salaryMax) { this.salaryMax = salaryMax; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public Integer getNumberOfOpenings() { return numberOfOpenings; }
    public void setNumberOfOpenings(Integer numberOfOpenings) { this.numberOfOpenings = numberOfOpenings; }

    public Integer getApplicantCount() { return applicantCount; }
    public void setApplicantCount(Integer applicantCount) { this.applicantCount = applicantCount; }

    public boolean isEasyApply() { return easyApply; }
    public void setEasyApply(boolean easyApply) { this.easyApply = easyApply; }

    public LocalDate getLastDateToApply() { return lastDateToApply; }
    public void setLastDateToApply(LocalDate lastDateToApply) { this.lastDateToApply = lastDateToApply; }

    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

    public JobStatus getJobStatus() { return status; }
    public void setJobStatus(JobStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
