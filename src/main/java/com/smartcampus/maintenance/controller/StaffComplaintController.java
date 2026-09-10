package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.dto.CommentRequest;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.exception.UnauthorizedOperationException;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
public class StaffComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/complaints")
    public String listAssignedTasks(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam(value = "status", required = false) ComplaintStatus status,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    Model model) {
        User staff = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Complaint> complaints = complaintService.getStaffComplaints(staff, status, pageable);

        model.addAttribute("complaints", complaints);
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(staff));

        return "staff/complaints_list";
    }

    @GetMapping("/completed")
    public String listCompletedTasks(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                     Model model) {
        User staff = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "resolvedAt"));

        Page<Complaint> complaints = complaintService.getStaffComplaints(staff, ComplaintStatus.RESOLVED, pageable);

        model.addAttribute("complaints", complaints);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(staff));

        return "staff/completed_list";
    }

    @GetMapping("/complaints/{id}")
    public String viewTask(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @PathVariable("id") Long id,
                           Model model) {
        User staff = userDetails.getUser();
        Complaint complaint = complaintService.getComplaintById(id);

        if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(staff.getId())) {
            throw new UnauthorizedOperationException("This complaint is not assigned to you");
        }

        model.addAttribute("complaint", complaint);
        model.addAttribute("history", complaintService.getComplaintHistory(complaint));
        model.addAttribute("comments", commentService.getCommentsForComplaint(complaint, staff));
        model.addAttribute("commentRequest", new CommentRequest());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(staff));

        return "staff/complaint_detail";
    }

    @PostMapping("/complaints/{id}/start")
    public String startWork(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @PathVariable("id") Long id,
                            RedirectAttributes redirectAttributes) {
        User staff = userDetails.getUser();
        complaintService.startWork(id, staff);
        redirectAttributes.addFlashAttribute("successMessage", "Work started! Complaint status updated to IN_PROGRESS.");
        return "redirect:/staff/complaints/" + id;
    }

    @PostMapping("/complaints/{id}/complete")
    public String completeWork(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("id") Long id,
                               @RequestParam(value = "afterImage", required = false) MultipartFile afterImage,
                               @RequestParam(value = "details", required = false) String details,
                               RedirectAttributes redirectAttributes) {
        User staff = userDetails.getUser();
        complaintService.completeWork(id, afterImage, details, staff);
        redirectAttributes.addFlashAttribute("successMessage", "Work marked as RESOLVED! Student has been notified to verify completion.");
        return "redirect:/staff/complaints/" + id;
    }

    @PostMapping("/complaints/{id}/comment")
    public String addComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable("id") Long id,
                             @Valid @ModelAttribute("commentRequest") CommentRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        User staff = userDetails.getUser();
        if (!bindingResult.hasErrors()) {
            commentService.addComment(id, request, staff);
            redirectAttributes.addFlashAttribute("successMessage", "Comment added successfully");
        }
        return "redirect:/staff/complaints/" + id;
    }
}
