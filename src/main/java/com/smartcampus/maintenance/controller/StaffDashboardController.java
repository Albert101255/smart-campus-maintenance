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
@RequestMapping("/staff")
public class StaffDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User staff = userDetails.getUser();

        DashboardStatsDto stats = dashboardService.getStaffDashboardStats(staff);
        model.addAttribute("stats", stats);
        model.addAttribute("recentTasks", complaintService.getRecentStaffComplaints(staff));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(staff));
        model.addAttribute("currentUser", staff);

        return "staff/dashboard";
    }
}
