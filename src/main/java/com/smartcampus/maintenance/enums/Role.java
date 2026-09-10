package com.smartcampus.maintenance.enums;

public enum Role {
    STUDENT("Student"),
    ADMIN("Administrator"),
    MAINTENANCE_STAFF("Maintenance Staff");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
