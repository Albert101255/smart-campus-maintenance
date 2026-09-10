package com.smartcampus.maintenance.dto;

import com.smartcampus.maintenance.enums.PriorityLevel;
import jakarta.validation.constraints.NotNull;

public class StaffAssignmentRequest {

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    private PriorityLevel priority;

    private String note;

    public StaffAssignmentRequest() {
    }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
