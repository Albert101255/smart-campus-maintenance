package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.dto.CommentRequest;
import com.smartcampus.maintenance.dto.StaffAssignmentRequest;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/complaints")
public class AdminComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listComplaints(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam(value = "status", required = false) ComplaintStatus status,
                                 @RequestParam(value = "priority", required = false) PriorityLevel priority,
                                 @RequestParam(value = "categoryId", required = false) Long categoryId,
                                 @RequestParam(value = "buildingId", required = false) Long buildingId,
                                 @RequestParam(value = "staffId", required = false) Long staffId,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        User admin = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Complaint> complaints = complaintService.filterComplaints(status, priority, categoryId, buildingId, staffId, null, keyword, pageable);

        model.addAttribute("complaints", complaints);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPriority", priority);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentBuildingId", buildingId);
        model.addAttribute("currentStaffId", staffId);
        model.addAttribute("keyword", keyword);

        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("priorities", PriorityLevel.values());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("buildings", buildingService.getAllBuildings());
        model.addAttribute("staffMembers", userService.getAllActiveStaffMembers());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/complaints_list";
    }

    @GetMapping("/{id}")
    public String viewComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable("id") Long id,
                                Model model) {
        User admin = userDetails.getUser();
        Complaint complaint = complaintService.getComplaintById(id);

        model.addAttribute("complaint", complaint);
        model.addAttribute("history", complaintService.getComplaintHistory(complaint));
        model.addAttribute("comments", commentService.getCommentsForComplaint(complaint, admin));
        model.addAttribute("commentRequest", new CommentRequest());
        model.addAttribute("assignmentRequest", new StaffAssignmentRequest());
        model.addAttribute("staffMembers", userService.getAllActiveStaffMembers());
        model.addAttribute("priorities", PriorityLevel.values());
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("feedback", feedbackService.getFeedbackForComplaint(complaint).orElse(null));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/complaint_detail";
    }

    @PostMapping("/{id}/assign")
    public String assignStaff(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable("id") Long id,
                              @Valid @ModelAttribute("assignmentRequest") StaffAssignmentRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        User admin = userDetails.getUser();
        if (!bindingResult.hasErrors()) {
            complaintService.assignStaff(id, request.getStaffId(), request.getPriority(), request.getNote(), admin);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance staff assigned successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign staff. Please select a valid staff member.");
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/{id}/priority")
    public String updatePriority(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable("id") Long id,
                                 @RequestParam("priority") PriorityLevel priority,
                                 RedirectAttributes redirectAttributes) {
        User admin = userDetails.getUser();
        complaintService.updatePriority(id, priority, admin);
        redirectAttributes.addFlashAttribute("successMessage", "Priority level updated to " + priority.getDisplayName());
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("id") Long id,
                               @RequestParam("status") ComplaintStatus status,
                               @RequestParam(value = "note", required = false) String note,
                               RedirectAttributes redirectAttributes) {
        User admin = userDetails.getUser();
        complaintService.updateStatus(id, status, note != null ? note : "Status updated by admin", admin);
        redirectAttributes.addFlashAttribute("successMessage", "Complaint status updated to " + status.getDisplayName());
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/{id}/comment")
    public String addComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable("id") Long id,
                             @Valid @ModelAttribute("commentRequest") CommentRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        User admin = userDetails.getUser();
        if (!bindingResult.hasErrors()) {
            commentService.addComment(id, request, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Note added successfully");
        }
        return "redirect:/admin/complaints/" + id;
    }
}
