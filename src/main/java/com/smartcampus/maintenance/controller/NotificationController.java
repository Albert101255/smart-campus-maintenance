package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.entity.Notification;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listNotifications(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "15") int size,
                                    Model model) {
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(user, pageable);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user));

        return "notifications/index";
    }

    @PostMapping("/{id}/read")
    public String markRead(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @PathVariable("id") Long id,
                           @RequestParam(value = "redirect", required = false) String redirect) {
        User user = userDetails.getUser();
        notificationService.markAsRead(id, user);
        return "redirect:" + (redirect != null ? redirect : "/notifications");
    }

    @PostMapping("/read-all")
    public String markAllRead(@AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        notificationService.markAllAsRead(user);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
