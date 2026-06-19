package com.hiresync.model;

public class JobFilterDTO {

    private String keyword;
    private String experienceLevel;
    private String employmentType;
    private String datePosted;
    private String location;
    private String workplaceType;
    private String companyName;
    private String industry;
    private Boolean easyApply;
    private Integer applicantCountLimit;
    private Double minimumSalary;
    private Boolean inYourNetwork;

    public JobFilterDTO() {}

    // Getters and Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getDatePosted() { return datePosted; }
    public void setDatePosted(String datePosted) { this.datePosted = datePosted; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWorkplaceType() { return workplaceType; }
    public void setWorkplaceType(String workplaceType) { this.workplaceType = workplaceType; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public Boolean getEasyApply() { return easyApply; }
    public void setEasyApply(Boolean easyApply) { this.easyApply = easyApply; }

    public Integer getApplicantCountLimit() { return applicantCountLimit; }
    public void setApplicantCountLimit(Integer applicantCountLimit) { this.applicantCountLimit = applicantCountLimit; }

    public Double getMinimumSalary() { return minimumSalary; }
    public void setMinimumSalary(Double minimumSalary) { this.minimumSalary = minimumSalary; }

    public Boolean getInYourNetwork() { return inYourNetwork; }
    public void setInYourNetwork(Boolean inYourNetwork) { this.inYourNetwork = inYourNetwork; }
}
