package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.entity.Feedback;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.FeedbackService;
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
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listFeedback(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        User admin = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Feedback> feedbackPage = feedbackService.getAllFeedback(pageable);

        model.addAttribute("feedbackPage", feedbackPage);
        model.addAttribute("avgRating", feedbackService.getAverageRating());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/feedback";
    }
}
