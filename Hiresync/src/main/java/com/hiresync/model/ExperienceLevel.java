package com.hiresync.model;

public enum ExperienceLevel {
    ENTRY_LEVEL("Entry Level"),
    MID_LEVEL("Mid Level"),
    SENIOR_LEVEL("Senior Level"),
    MANAGER("Manager"),
    EXECUTIVE("Executive");

    private final String displayName;

    ExperienceLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ExperienceLevel fromString(String text) {
        if (text == null) return ENTRY_LEVEL;
        String normalized = text.trim().replace(" ", "_").toUpperCase();
        for (ExperienceLevel el : values()) {
            if (el.name().equals(normalized) || el.getDisplayName().equalsIgnoreCase(text)) {
                return el;
            }
        }
        return ENTRY_LEVEL;
    }
}
