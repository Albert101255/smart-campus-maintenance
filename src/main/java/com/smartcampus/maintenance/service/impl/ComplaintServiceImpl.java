package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.dto.ComplaintCreateRequest;
import com.smartcampus.maintenance.entity.*;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import com.smartcampus.maintenance.enums.Role;
import com.smartcampus.maintenance.exception.ComplaintNotFoundException;
import com.smartcampus.maintenance.exception.UnauthorizedOperationException;
import com.smartcampus.maintenance.repository.*;
import com.smartcampus.maintenance.service.*;
import com.smartcampus.maintenance.util.ComplaintNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ComplaintHistoryRepository historyRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Complaint createComplaint(ComplaintCreateRequest request, User student) {
        Category category = categoryService.getById(request.getCategoryId());
        Building building = buildingService.getById(request.getBuildingId());

        long count = complaintRepository.count() + 1;
        String complaintNumber = ComplaintNumberGenerator.generateComplaintNumber(count);

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(complaintNumber);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(category);
        complaint.setBuilding(building);
        complaint.setFloor(request.getFloor());
        complaint.setRoomNumber(request.getRoomNumber());
        complaint.setLocationDescription(request.getLocationDescription());
        complaint.setPriority(request.getPriority() != null ? request.getPriority() : PriorityLevel.MEDIUM);
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setStudent(student);

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imagePath = fileStorageService.storeFile(request.getImage(), "complaints");
            complaint.setBeforeImagePath(imagePath);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Record history
        ComplaintHistory history = new ComplaintHistory(savedComplaint, student, "SUBMITTED", null, ComplaintStatus.OPEN, "Complaint submitted by student");
        historyRepository.save(history);

        // Notify Student
        notificationService.sendNotification(student, "Complaint Submitted",
                "Your complaint " + complaintNumber + " has been submitted successfully.",
                "/student/complaints/" + savedComplaint.getId());

        // Notify Admins
        List<User> admins = userRepository.findByRoleAndEnabledTrue(Role.ADMIN);
        for (User admin : admins) {
            notificationService.sendNotification(admin, "New Complaint Submitted",
                    "New complaint " + complaintNumber + " submitted in " + building.getName(),
                    "/admin/complaints/" + savedComplaint.getId());
        }

        auditService.log(student, "CREATE_COMPLAINT", "Complaint", savedComplaint.getId(), "Created complaint " + complaintNumber);

        return savedComplaint;
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint getComplaintByNumber(String complaintNumber) {
        return complaintRepository.findByComplaintNumber(complaintNumber)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with number: " + complaintNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Complaint> getStudentComplaints(User student, Pageable pageable) {
        return complaintRepository.findByStudent(student, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Complaint> getStaffComplaints(User staff, ComplaintStatus status, Pageable pageable) {
        if (status != null) {
            return complaintRepository.findByAssignedStaffAndStatus(staff, status, pageable);
        }
        return complaintRepository.findByAssignedStaff(staff, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Complaint> filterComplaints(ComplaintStatus status, PriorityLevel priority, Long categoryId,
                                             Long buildingId, Long staffId, Long studentId, String keyword, Pageable pageable) {
        return complaintRepository.filterComplaints(status, priority, categoryId, buildingId, staffId, studentId, keyword, pageable);
    }

    @Override
    public Complaint assignStaff(Long complaintId, Long staffId, PriorityLevel priority, String note, User admin) {
        Complaint complaint = getComplaintById(complaintId);
        User staff = userService.findById(staffId);

        if (staff.getRole() != Role.MAINTENANCE_STAFF) {
            throw new IllegalArgumentException("Assigned user must have MAINTENANCE_STAFF role");
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setAssignedStaff(staff);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        complaint.setAssignedAt(LocalDateTime.now());

        if (priority != null) {
            complaint.setPriority(priority);
        }

        Complaint saved = complaintRepository.save(complaint);

        String details = "Assigned to technician " + staff.getName() + (note != null && !note.isBlank() ? ". Note: " + note : "");
        ComplaintHistory history = new ComplaintHistory(saved, admin, "ASSIGNED_STAFF", oldStatus, ComplaintStatus.ASSIGNED, details);
        historyRepository.save(history);

        // Notify Maintenance Staff
        notificationService.sendNotification(staff, "New Task Assigned",
                "You have been assigned to complaint " + saved.getComplaintNumber() + " (" + saved.getTitle() + ")",
                "/staff/complaints/" + saved.getId());

        // Notify Student
        notificationService.sendNotification(saved.getStudent(), "Staff Assigned",
                "Maintenance technician " + staff.getName() + " has been assigned to your complaint " + saved.getComplaintNumber(),
                "/student/complaints/" + saved.getId());

        auditService.log(admin, "ASSIGN_STAFF", "Complaint", saved.getId(), details);

        return saved;
    }

    @Override
    public Complaint updateStatus(Long complaintId, ComplaintStatus newStatus, String details, User currentUser) {
        Complaint complaint = getComplaintById(complaintId);
        ComplaintStatus oldStatus = complaint.getStatus();

        if (oldStatus == newStatus) {
            return complaint;
        }

        complaint.setStatus(newStatus);
        if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        } else if (newStatus == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(LocalDateTime.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        ComplaintHistory history = new ComplaintHistory(saved, currentUser, "STATUS_UPDATE", oldStatus, newStatus, details);
        historyRepository.save(history);

        // Notify Student
        notificationService.sendNotification(saved.getStudent(), "Status Updated: " + newStatus.getDisplayName(),
                "Your complaint " + saved.getComplaintNumber() + " status is now " + newStatus.getDisplayName(),
                "/student/complaints/" + saved.getId());

        auditService.log(currentUser, "UPDATE_STATUS", "Complaint", saved.getId(), "Status changed from " + oldStatus + " to " + newStatus);

        return saved;
    }

    @Override
    public Complaint updatePriority(Long complaintId, PriorityLevel priority, User admin) {
        Complaint complaint = getComplaintById(complaintId);
        PriorityLevel oldPriority = complaint.getPriority();
        complaint.setPriority(priority);

        Complaint saved = complaintRepository.save(complaint);

        ComplaintHistory history = new ComplaintHistory(saved, admin, "PRIORITY_CHANGE", complaint.getStatus(), complaint.getStatus(),
                "Priority changed from " + oldPriority + " to " + priority);
        historyRepository.save(history);

        auditService.log(admin, "UPDATE_PRIORITY", "Complaint", saved.getId(), "Priority changed from " + oldPriority + " to " + priority);

        return saved;
    }

    @Override
    public Complaint startWork(Long complaintId, User staff) {
        Complaint complaint = getComplaintById(complaintId);
        if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(staff.getId())) {
            throw new UnauthorizedOperationException("You are not assigned to this complaint");
        }

        return updateStatus(complaintId, ComplaintStatus.IN_PROGRESS, "Maintenance work started by technician " + staff.getName(), staff);
    }

    @Override
    public Complaint completeWork(Long complaintId, MultipartFile afterImage, String details, User staff) {
        Complaint complaint = getComplaintById(complaintId);
        if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(staff.getId())) {
            throw new UnauthorizedOperationException("You are not assigned to this complaint");
        }

        if (afterImage != null && !afterImage.isEmpty()) {
            String afterImagePath = fileStorageService.storeFile(afterImage, "repairs");
            complaint.setAfterImagePath(afterImagePath);
        }

        String completeDetails = "Maintenance completed by technician " + staff.getName() + (details != null && !details.isBlank() ? ": " + details : "");
        return updateStatus(complaintId, ComplaintStatus.RESOLVED, completeDetails, staff);
    }

    @Override
    public Complaint confirmResolution(Long complaintId, User student) {
        Complaint complaint = getComplaintById(complaintId);
        if (!complaint.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedOperationException("You can only confirm resolution for your own complaints");
        }

        return updateStatus(complaintId, ComplaintStatus.CLOSED, "Student confirmed issue resolution", student);
    }

    @Override
    public Complaint reopenComplaint(Long complaintId, String reason, User student) {
        Complaint complaint = getComplaintById(complaintId);
        if (!complaint.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedOperationException("You can only reopen your own complaints");
        }

        String reopenDetails = "Student reopened complaint. Reason: " + (reason != null ? reason : "Issue persists");
        return updateStatus(complaintId, ComplaintStatus.REOPENED, reopenDetails, student);
    }

    @Override
    public Complaint cancelComplaint(Long complaintId, String reason, User user) {
        Complaint complaint = getComplaintById(complaintId);
        if (user.getRole() == Role.STUDENT && !complaint.getStudent().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("You can only cancel your own complaints");
        }

        String cancelDetails = "Complaint cancelled. Reason: " + (reason != null ? reason : "Cancelled by user");
        return updateStatus(complaintId, ComplaintStatus.CANCELLED, cancelDetails, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintHistory> getComplaintHistory(Complaint complaint) {
        return historyRepository.findByComplaintOrderByTimestampAsc(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> getRecentComplaints() {
        return complaintRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> getRecentStudentComplaints(User student) {
        return complaintRepository.findTop5ByStudentOrderByCreatedAtDesc(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> getRecentStaffComplaints(User staff) {
        return complaintRepository.findTop5ByAssignedStaffOrderByCreatedAtDesc(staff);
    }
}
