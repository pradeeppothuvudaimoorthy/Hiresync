package com.hiresync.model;

public enum DatePostedFilter {
    LAST_24_HOURS("Last 24 Hours"),
    LAST_7_DAYS("Past Week"),
    LAST_30_DAYS("Past Month"),
    ANY_TIME("Any Time");

    private final String displayName;

    DatePostedFilter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DatePostedFilter fromString(String text) {
        if (text == null) return ANY_TIME;
        String normalized = text.trim().replace(" ", "_").toUpperCase();
        for (DatePostedFilter dpf : values()) {
            if (dpf.name().equals(normalized) || dpf.getDisplayName().equalsIgnoreCase(text)) {
                return dpf;
            }
        }
        return ANY_TIME;
    }
}
