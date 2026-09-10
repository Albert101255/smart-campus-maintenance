package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.dto.DashboardStatsDto;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.ComplaintService;
import com.smartcampus.maintenance.service.DashboardService;
import com.smartcampus.maintenance.service.NotificationService;
import com.smartcampus.maintenance.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User admin = userDetails.getUser();

        DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentComplaints", complaintService.getRecentComplaints());
        model.addAttribute("categoryChartData", reportService.getComplaintsByCategory());
        model.addAttribute("statusChartData", reportService.getComplaintsByStatus());
        model.addAttribute("priorityChartData", reportService.getComplaintsByPriority());
        model.addAttribute("buildingChartData", reportService.getComplaintsByBuilding());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/dashboard";
    }
}
