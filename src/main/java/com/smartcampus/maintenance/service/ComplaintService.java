package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.dto.ComplaintCreateRequest;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.ComplaintHistory;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ComplaintService {
    Complaint createComplaint(ComplaintCreateRequest request, User student);
    Complaint getComplaintById(Long id);
    Complaint getComplaintByNumber(String complaintNumber);
    Page<Complaint> getStudentComplaints(User student, Pageable pageable);
    Page<Complaint> getStaffComplaints(User staff, ComplaintStatus status, Pageable pageable);
    Page<Complaint> filterComplaints(ComplaintStatus status, PriorityLevel priority, Long categoryId,
                                     Long buildingId, Long staffId, Long studentId, String keyword, Pageable pageable);
    Complaint assignStaff(Long complaintId, Long staffId, PriorityLevel priority, String note, User admin);
    Complaint updateStatus(Long complaintId, ComplaintStatus newStatus, String details, User currentUser);
    Complaint updatePriority(Long complaintId, PriorityLevel priority, User admin);
    Complaint startWork(Long complaintId, User staff);
    Complaint completeWork(Long complaintId, MultipartFile afterImage, String details, User staff);
    Complaint confirmResolution(Long complaintId, User student);
    Complaint reopenComplaint(Long complaintId, String reason, User student);
    Complaint cancelComplaint(Long complaintId, String reason, User user);
    List<ComplaintHistory> getComplaintHistory(Complaint complaint);
    List<Complaint> getRecentComplaints();
    List<Complaint> getRecentStudentComplaints(User student);
    List<Complaint> getRecentStaffComplaints(User staff);
}
