package com.hiresync.model;

public enum WorkplaceType {
    REMOTE("Remote"),
    ONSITE("On-site"),
    HYBRID("Hybrid");

    private final String displayName;

    WorkplaceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static WorkplaceType fromString(String text) {
        if (text == null) return ONSITE;
        String normalized = text.trim().replace("-", "").toUpperCase();
        for (WorkplaceType wt : values()) {
            if (wt.name().equals(normalized) || wt.getDisplayName().equalsIgnoreCase(text)) {
                return wt;
            }
        }
        return ONSITE;
    }
}
