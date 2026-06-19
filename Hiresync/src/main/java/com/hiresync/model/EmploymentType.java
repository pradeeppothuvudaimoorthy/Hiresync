package com.hiresync.model;

public enum EmploymentType {
    FULL_TIME("Full-time"),
    PART_TIME("Part-time"),
    CONTRACT("Contract"),
    FREELANCE("Freelance"),
    INTERNSHIP("Internship");

    private final String displayName;

    EmploymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EmploymentType fromString(String text) {
        if (text == null) return FULL_TIME;
        String normalized = text.trim().replace("-", "_").replace(" ", "_").toUpperCase();
        for (EmploymentType et : values()) {
            if (et.name().equals(normalized) || et.getDisplayName().equalsIgnoreCase(text)) {
                return et;
            }
        }
        return FULL_TIME;
    }
}
