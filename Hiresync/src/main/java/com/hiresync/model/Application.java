package com.hiresync.model;

import com.hiresync.entity.Resume;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", referencedColumnName = "id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", referencedColumnName = "id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    private Resume resume;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "match_score")
    private Double matchScore; // Percentage, e.g. 60.0

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false)
    private MatchStatus matchStatus = MatchStatus.NOT_ANALYZED;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "recruiter_notes", columnDefinition = "TEXT")
    private String recruiterNotes;

    @Column(name = "candidate_notes", columnDefinition = "TEXT")
    private String candidateNotes;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_withdrawn", nullable = false)
    private boolean isWithdrawn = false;

    public Application() {}

    @PrePersist
    protected void onCreate() {
        this.applicationDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApplicationStatus.APPLIED;
        }
        if (this.matchStatus == null) {
            this.matchStatus = MatchStatus.NOT_ANALYZED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Backwards Compatibility Accessors (Aliased Getters/Setters) ---
    public String getStatus() {
        return this.status != null ? this.status.getDisplayName() : "Applied";
    }

    public void setStatus(String statusStr) {
        this.status = ApplicationStatus.fromString(statusStr);
    }

    public String getMatchStatus() {
        return this.matchStatus != null ? this.matchStatus.getDisplayName() : "Not Analyzed";
    }

    public void setMatchStatus(String statusStr) {
        this.matchStatus = MatchStatus.fromString(statusStr);
    }

    // --- Standard Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }

    public ApplicationStatus getApplicationStatus() { return status; }
    public void setApplicationStatus(ApplicationStatus status) { this.status = status; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public MatchStatus getMatchStatusEnum() { return matchStatus; }
    public void setMatchStatusEnum(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getRecruiterNotes() { return recruiterNotes; }
    public void setRecruiterNotes(String recruiterNotes) { this.recruiterNotes = recruiterNotes; }

    public String getCandidateNotes() { return candidateNotes; }
    public void setCandidateNotes(String candidateNotes) { this.candidateNotes = candidateNotes; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isWithdrawn() { return isWithdrawn; }
    public void setWithdrawn(boolean withdrawn) { isWithdrawn = withdrawn; }
}
