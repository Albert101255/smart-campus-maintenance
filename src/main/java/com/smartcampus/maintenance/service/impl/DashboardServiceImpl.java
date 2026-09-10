package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.dto.DashboardStatsDto;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import com.smartcampus.maintenance.enums.Role;
import com.smartcampus.maintenance.repository.ComplaintRepository;
import com.smartcampus.maintenance.repository.UserRepository;
import com.smartcampus.maintenance.service.DashboardService;
import com.smartcampus.maintenance.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackService feedbackService;

    @Override
    public DashboardStatsDto getAdminDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalComplaints(complaintRepository.count());
        stats.setOpenComplaints(complaintRepository.countByStatus(ComplaintStatus.OPEN));
        stats.setAssignedComplaints(complaintRepository.countByStatus(ComplaintStatus.ASSIGNED));
        stats.setInProgressComplaints(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS));
        stats.setOnHoldComplaints(complaintRepository.countByStatus(ComplaintStatus.ON_HOLD));
        stats.setResolvedComplaints(complaintRepository.countByStatus(ComplaintStatus.RESOLVED));
        stats.setClosedComplaints(complaintRepository.countByStatus(ComplaintStatus.CLOSED));
        stats.setCriticalComplaints(complaintRepository.countByPriority(PriorityLevel.CRITICAL));

        stats.setTotalStudents(userRepository.countByRole(Role.STUDENT));
        stats.setTotalStaff(userRepository.countByRole(Role.MAINTENANCE_STAFF));
        stats.setAvgRating(feedbackService.getAverageRating());

        List<Complaint> allActive = complaintRepository.findAll();
        long overdueCount = allActive.stream().filter(Complaint::isOverdue).count();
        stats.setOverdueComplaints(overdueCount);

        return stats;
    }

    @Override
    public DashboardStatsDto getStudentDashboardStats(User student) {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalComplaints(complaintRepository.countByStudent(student));
        stats.setOpenComplaints(complaintRepository.countByStudentAndStatus(student, ComplaintStatus.OPEN));
        stats.setInProgressComplaints(complaintRepository.countByStudentAndStatus(student, ComplaintStatus.IN_PROGRESS));
        stats.setResolvedComplaints(complaintRepository.countByStudentAndStatus(student, ComplaintStatus.RESOLVED));
        stats.setClosedComplaints(complaintRepository.countByStudentAndStatus(student, ComplaintStatus.CLOSED));

        return stats;
    }

    @Override
    public DashboardStatsDto getStaffDashboardStats(User staff) {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalComplaints(complaintRepository.countByAssignedStaff(staff));
        stats.setAssignedComplaints(complaintRepository.countByAssignedStaffAndStatus(staff, ComplaintStatus.ASSIGNED));
        stats.setInProgressComplaints(complaintRepository.countByAssignedStaffAndStatus(staff, ComplaintStatus.IN_PROGRESS));
        stats.setResolvedComplaints(complaintRepository.countByAssignedStaffAndStatus(staff, ComplaintStatus.RESOLVED));

        return stats;
    }
}
