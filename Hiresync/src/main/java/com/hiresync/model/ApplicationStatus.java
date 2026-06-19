package com.hiresync.model;

public enum ApplicationStatus {
    APPLIED("Applied"),
    UNDER_REVIEW("Under Review"),
    SHORTLISTED("Shortlisted"),
    REJECTED("Rejected"),
    INTERVIEW_SCHEDULED("Interview Scheduled"),
    SELECTED("Selected"),
    WITHDRAWN("Withdrawn");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ApplicationStatus fromString(String text) {
        if (text == null) return APPLIED;
        String normalized = text.trim().replace(" ", "_").toUpperCase();
        for (ApplicationStatus as : values()) {
            if (as.name().equals(normalized) || as.getDisplayName().equalsIgnoreCase(text)) {
                return as;
            }
        }
        return APPLIED;
    }
}
