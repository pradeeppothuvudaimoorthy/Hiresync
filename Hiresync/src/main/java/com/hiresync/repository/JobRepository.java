package com.hiresync.repository;

import com.hiresync.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByRecruiterId(Long recruiterId);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByStatusAndJobTitleContainingIgnoreCase(JobStatus status, String keyword);

    List<Job> findByStatusAndCompanyNameContainingIgnoreCase(JobStatus status, String companyName);

    List<Job> findByStatusAndJobLocationContainingIgnoreCase(JobStatus status, String location);

    List<Job> findByStatusAndRequiredSkillsContainingIgnoreCase(JobStatus status, String skill);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.jobLocation) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Job> searchJobs(@Param("query") String query);

    @Query("SELECT j FROM Job j WHERE j.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(j.jobDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(j.jobLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
           ") " +
           "AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) " +
           "AND (:employmentType IS NULL OR j.employmentType = :employmentType) " +
           "AND (:workplaceType IS NULL OR j.workplaceType = :workplaceType) " +
           "AND (:location IS NULL OR :location = '' OR " +
           "     LOWER(j.jobLocation) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "     LOWER(j.city) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "     LOWER(j.state) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "     LOWER(j.country) LIKE LOWER(CONCAT('%', :location, '%'))" +
           ") " +
           "AND (:companyName IS NULL OR :companyName = '' OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) " +
           "AND (:industry IS NULL OR :industry = '' OR LOWER(j.industry) LIKE LOWER(CONCAT('%', :industry, '%'))) " +
           "AND (:easyApply IS NULL OR j.easyApply = :easyApply) " +
           "AND (:applicantCountLimit IS NULL OR j.applicantCount <= :applicantCountLimit) " +
           "AND (:minimumSalary IS NULL OR j.salaryMax >= :minimumSalary OR j.salaryMin >= :minimumSalary) " +
           "AND (:startDate IS NULL OR j.postedDate >= :startDate)")
    List<Job> searchJobsWithFilters(
            @Param("status") JobStatus status,
            @Param("keyword") String keyword,
            @Param("experienceLevel") ExperienceLevel experienceLevel,
            @Param("employmentType") EmploymentType employmentType,
            @Param("workplaceType") WorkplaceType workplaceType,
            @Param("location") String location,
            @Param("companyName") String companyName,
            @Param("industry") String industry,
            @Param("easyApply") Boolean easyApply,
            @Param("applicantCountLimit") Integer applicantCountLimit,
            @Param("minimumSalary") Double minimumSalary,
            @Param("startDate") LocalDate startDate
    );
}
