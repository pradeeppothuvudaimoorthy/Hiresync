package com.hiresync.repository;

import com.hiresync.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplicationCandidateId(Long candidateId);
    List<Interview> findByApplicationJobRecruiterId(Long recruiterId);
    Optional<Interview> findByApplicationId(Long applicationId);
}
