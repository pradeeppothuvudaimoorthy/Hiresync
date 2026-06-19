package com.hiresync.model;

public enum MatchStatus {
    HIGH_MATCH("High Match"),
    MEDIUM_MATCH("Medium Match"),
    LOW_MATCH("Low Match"),
    NOT_ANALYZED("Not Analyzed");

    private final String displayName;

    MatchStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MatchStatus fromString(String text) {
        if (text == null) return NOT_ANALYZED;
        String normalized = text.trim().replace(" ", "_").toUpperCase();
        for (MatchStatus ms : values()) {
            if (ms.name().equals(normalized) || ms.getDisplayName().equalsIgnoreCase(text)) {
                return ms;
            }
        }
        return NOT_ANALYZED;
    }
}
