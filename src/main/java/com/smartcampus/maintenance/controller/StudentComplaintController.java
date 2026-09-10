package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.dto.CommentRequest;
import com.smartcampus.maintenance.dto.ComplaintCreateRequest;
import com.smartcampus.maintenance.dto.FeedbackRequest;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student/complaints")
public class StudentComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listComplaints(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam(value = "status", required = false) ComplaintStatus status,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        User student = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Complaint> complaints = complaintService.filterComplaints(status, null, null, null, null, student.getId(), keyword, pageable);

        model.addAttribute("complaints", complaints);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));

        return "student/complaints_list";
    }

    @GetMapping("/new")
    public String newComplaintForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();

        model.addAttribute("complaintCreateRequest", new ComplaintCreateRequest());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("buildings", buildingService.getAllActiveBuildings());
        model.addAttribute("priorities", PriorityLevel.values());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));

        return "student/complaint_new";
    }

    @PostMapping("/new")
    public String createComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @ModelAttribute("complaintCreateRequest") ComplaintCreateRequest request,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        User student = userDetails.getUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActiveCategories());
            model.addAttribute("buildings", buildingService.getAllActiveBuildings());
            model.addAttribute("priorities", PriorityLevel.values());
            model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
            return "student/complaint_new";
        }

        Complaint complaint = complaintService.createComplaint(request, student);
        redirectAttributes.addFlashAttribute("successMessage", "Complaint " + complaint.getComplaintNumber() + " submitted successfully!");

        return "redirect:/student/complaints/" + complaint.getId();
    }

    @GetMapping("/{id}")
    public String viewComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable("id") Long id,
                                Model model) {
        User student = userDetails.getUser();
        Complaint complaint = complaintService.getComplaintById(id);

        if (!complaint.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedOperationException("You are not authorized to view this complaint");
        }

        model.addAttribute("complaint", complaint);
        model.addAttribute("history", complaintService.getComplaintHistory(complaint));
        model.addAttribute("comments", commentService.getCommentsForComplaint(complaint, student));
        model.addAttribute("commentRequest", new CommentRequest());
        model.addAttribute("feedbackRequest", new FeedbackRequest());
        model.addAttribute("feedback", feedbackService.getFeedbackForComplaint(complaint).orElse(null));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));

        return "student/complaint_detail";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable("id") Long id,
                             @Valid @ModelAttribute("commentRequest") CommentRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        User student = userDetails.getUser();
        if (!bindingResult.hasErrors()) {
            commentService.addComment(id, request, student);
            redirectAttributes.addFlashAttribute("successMessage", "Comment added successfully");
        }
        return "redirect:/student/complaints/" + id;
    }

    @PostMapping("/{id}/confirm")
    public String confirmResolution(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable("id") Long id,
                                    RedirectAttributes redirectAttributes) {
        User student = userDetails.getUser();
        complaintService.confirmResolution(id, student);
        redirectAttributes.addFlashAttribute("successMessage", "Resolution confirmed! Thank you for helping keep our campus safe and functional.");
        return "redirect:/student/complaints/" + id;
    }

    @PostMapping("/{id}/reopen")
    public String reopenComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("id") Long id,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  RedirectAttributes redirectAttributes) {
        User student = userDetails.getUser();
        complaintService.reopenComplaint(id, reason, student);
        redirectAttributes.addFlashAttribute("successMessage", "Complaint reopened. Our team will re-evaluate the issue.");
        return "redirect:/student/complaints/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("id") Long id,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  RedirectAttributes redirectAttributes) {
        User student = userDetails.getUser();
        complaintService.cancelComplaint(id, reason, student);
        redirectAttributes.addFlashAttribute("successMessage", "Complaint cancelled.");
        return "redirect:/student/complaints/" + id;
    }

    @PostMapping("/{id}/feedback")
    public String submitFeedback(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable("id") Long id,
                                 @Valid @ModelAttribute("feedbackRequest") FeedbackRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        User student = userDetails.getUser();
        if (!bindingResult.hasErrors()) {
            feedbackService.submitFeedback(id, request, student);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your feedback!");
        }
        return "redirect:/student/complaints/" + id;
    }
}
