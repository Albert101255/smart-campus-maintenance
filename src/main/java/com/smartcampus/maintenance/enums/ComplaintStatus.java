package com.smartcampus.maintenance.enums;

public enum ComplaintStatus {
    OPEN("Open", "bg-warning text-dark", "Complaint received, awaiting review"),
    ASSIGNED("Assigned", "bg-info text-dark", "Assigned to maintenance technician"),
    IN_PROGRESS("In Progress", "bg-primary", "Maintenance work is currently underway"),
    ON_HOLD("On Hold", "bg-secondary", "Work temporarily paused pending parts/access"),
    RESOLVED("Resolved", "bg-success", "Maintenance work completed, awaiting student verification"),
    CLOSED("Closed", "bg-dark", "Issue resolution confirmed and closed"),
    REOPENED("Reopened", "bg-danger", "Issue reported as unresolved by student"),
    CANCELLED("Cancelled", "bg-light text-dark", "Complaint cancelled");

    private final String displayName;
    private final String badgeClass;
    private final String description;

    ComplaintStatus(String displayName, String badgeClass, String description) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getDescription() {
        return description;
    }
}
