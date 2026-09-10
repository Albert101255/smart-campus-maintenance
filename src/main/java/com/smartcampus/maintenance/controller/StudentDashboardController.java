package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.dto.DashboardStatsDto;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.ComplaintService;
import com.smartcampus.maintenance.service.DashboardService;
import com.smartcampus.maintenance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();

        DashboardStatsDto stats = dashboardService.getStudentDashboardStats(student);
        model.addAttribute("stats", stats);
        model.addAttribute("recentComplaints", complaintService.getRecentStudentComplaints(student));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
        model.addAttribute("currentUser", student);

        return "student/dashboard";
    }
}
