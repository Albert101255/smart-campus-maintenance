package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.dto.StaffCreationRequest;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.NotificationService;
import com.smartcampus.maintenance.service.UserService;
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
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/staff")
    public String listStaff(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        User admin = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<User> staffPage = userService.getStaffMembers(pageable);

        model.addAttribute("staffPage", staffPage);
        model.addAttribute("staffCreationRequest", new StaffCreationRequest());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/staff_list";
    }

    @PostMapping("/staff")
    public String createStaff(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @Valid @ModelAttribute("staffCreationRequest") StaffCreationRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User admin = userDetails.getUser();

        if (bindingResult.hasErrors()) {
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
            model.addAttribute("staffPage", userService.getStaffMembers(pageable));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));
            return "admin/staff_list";
        }

        try {
            userService.createStaffMember(request);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance staff member account created successfully!");
            return "redirect:/admin/staff";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/staff";
        }
    }

    @GetMapping("/students")
    public String listStudents(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        User admin = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<User> studentsPage = userService.getStudents(search, pageable);

        model.addAttribute("studentsPage", studentsPage);
        model.addAttribute("search", search);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        return "admin/students_list";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.toggleUserStatus(id);
        String status = user.isEnabled() ? "enabled" : "disabled";
        redirectAttributes.addFlashAttribute("successMessage", "Account for " + user.getName() + " has been " + status + ".");
        return "redirect:" + (user.getRole().name().equals("STUDENT") ? "/admin/students" : "/admin/staff");
    }
}
