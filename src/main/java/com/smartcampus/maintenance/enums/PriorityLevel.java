package com.smartcampus.maintenance.enums;

public enum PriorityLevel {
    LOW("Low", "Normal", "bg-info text-dark", 48),
    MEDIUM("Medium", "Attention", "bg-warning text-dark", 24),
    HIGH("High", "Urgent", "bg-orange text-white", 12),
    CRITICAL("Critical", "Immediate Action", "bg-danger text-white", 4);

    private final String displayName;
    private final String urgencyText;
    private final String badgeClass;
    private final int slaHours;

    PriorityLevel(String displayName, String urgencyText, String badgeClass, int slaHours) {
        this.displayName = displayName;
        this.urgencyText = urgencyText;
        this.badgeClass = badgeClass;
        this.slaHours = slaHours;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrgencyText() {
        return urgencyText;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public int getSlaHours() {
        return slaHours;
    }
}
