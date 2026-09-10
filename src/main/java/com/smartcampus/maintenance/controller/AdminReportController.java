package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.repository.ComplaintRepository;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.NotificationService;
import com.smartcampus.maintenance.service.ReportService;
import com.smartcampus.maintenance.util.CsvExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String reportsOverview(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User admin = userDetails.getUser();

        model.addAttribute("byCategory", reportService.getComplaintsByCategory());
        model.addAttribute("byStatus", reportService.getComplaintsByStatus());
        model.addAttribute("byPriority", reportService.getComplaintsByPriority());
        model.addAttribute("byBuilding", reportService.getComplaintsByBuilding());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/reports";
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"campus_complaints_report.csv\"");

        List<Complaint> complaints = complaintRepository.findAll();
        CsvExportUtil.writeComplaintsToCsv(response.getWriter(), complaints);
    }
}
