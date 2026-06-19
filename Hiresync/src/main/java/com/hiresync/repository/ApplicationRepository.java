package com.hiresync.repository;

import com.hiresync.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateId(Long candidateId);
    List<Application> findByJobRecruiterId(Long recruiterId);
    
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
    
    long countByJobRecruiterId(Long recruiterId);
    long countByJobRecruiterIdAndStatus(Long recruiterId, String status);
    
    // Recent 5 applications for recruiter dashboard
    List<Application> findTop5ByJobRecruiterIdOrderByIdDesc(Long recruiterId);
}
