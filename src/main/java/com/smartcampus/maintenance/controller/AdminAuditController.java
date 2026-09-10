package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.entity.AuditLog;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.AuditService;
import com.smartcampus.maintenance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit")
public class AdminAuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String viewAuditLogs(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "15") int size,
                                Model model) {
        User admin = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogs(pageable);

        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/audit";
    }
}
