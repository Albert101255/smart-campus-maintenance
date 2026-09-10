package com.smartcampus.maintenance.dto;

public class DashboardStatsDto {

    private long totalComplaints;
    private long openComplaints;
    private long assignedComplaints;
    private long inProgressComplaints;
    private long onHoldComplaints;
    private long resolvedComplaints;
    private long closedComplaints;
    private long criticalComplaints;
    private long overdueComplaints;
    private long totalStudents;
    private long totalStaff;
    private double avgRating;

    public DashboardStatsDto() {
    }

    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }

    public long getOpenComplaints() { return openComplaints; }
    public void setOpenComplaints(long openComplaints) { this.openComplaints = openComplaints; }

    public long getAssignedComplaints() { return assignedComplaints; }
    public void setAssignedComplaints(long assignedComplaints) { this.assignedComplaints = assignedComplaints; }

    public long getInProgressComplaints() { return inProgressComplaints; }
    public void setInProgressComplaints(long inProgressComplaints) { this.inProgressComplaints = inProgressComplaints; }

    public long getOnHoldComplaints() { return onHoldComplaints; }
    public void setOnHoldComplaints(long onHoldComplaints) { this.onHoldComplaints = onHoldComplaints; }

    public long getResolvedComplaints() { return resolvedComplaints; }
    public void setResolvedComplaints(long resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }

    public long getClosedComplaints() { return closedComplaints; }
    public void setClosedComplaints(long closedComplaints) { this.closedComplaints = closedComplaints; }

    public long getCriticalComplaints() { return criticalComplaints; }
    public void setCriticalComplaints(long criticalComplaints) { this.criticalComplaints = criticalComplaints; }

    public long getOverdueComplaints() { return overdueComplaints; }
    public void setOverdueComplaints(long overdueComplaints) { this.overdueComplaints = overdueComplaints; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalStaff() { return totalStaff; }
    public void setTotalStaff(long totalStaff) { this.totalStaff = totalStaff; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
}
